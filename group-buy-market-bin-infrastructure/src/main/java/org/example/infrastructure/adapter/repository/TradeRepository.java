package org.example.infrastructure.adapter.repository;

import com.alibaba.fastjson.JSON;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.example.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import org.example.domain.trade.adapter.repository.ITradeRepository;
import org.example.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import org.example.domain.trade.model.aggregate.GroupBuyRefundAggregate;
import org.example.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import org.example.domain.trade.model.entity.*;
import org.example.domain.trade.model.valobj.*;
import org.example.infrastructure.dao.IGroupBuyActivityDao;
import org.example.infrastructure.dao.IGroupBuyOrderDao;
import org.example.infrastructure.dao.IGroupBuyOrderListDao;
import org.example.infrastructure.dao.INotifyTaskDao;
import org.example.infrastructure.dao.po.GroupBuyActivity;
import org.example.infrastructure.dao.po.GroupBuyOrder;
import org.example.infrastructure.dao.po.GroupBuyOrderList;
import org.example.infrastructure.dao.po.NotifyTask;
import org.example.infrastructure.dcc.DCCService;
import org.example.infrastructure.redis.IRedisService;
import org.example.types.common.Constants;
import org.example.types.enums.ActivityStatusEnumVO;
import org.example.types.enums.GroupBuyOrderEnumVO;
import org.example.types.enums.ResponseCode;
import org.example.types.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName : TradeRepository
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/6  11:16
 */
@Slf4j
@Repository
public class TradeRepository implements ITradeRepository {

    @Resource
    private IGroupBuyOrderDao groupBuyOrderDao;
    @Resource
    private IGroupBuyOrderListDao groupBuyOrderListDao;
    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;
    @Resource
    private INotifyTaskDao notifyTaskDao;
    @Resource
    private DCCService dccService;

    @Value("${spring.rabbitmq.config.producer.topic_team_success.routing_key}")
    private String topic_team_success;

    @Value("${spring.rabbitmq.config.producer.topic_team_refund.routing_key}")
    private String topic_team_refund;

    @Resource
    private IRedisService redisService;

    @Override
    public MarketPayOrderEntity queryMarketPayOrderByOutTradeNo(String userId, String outTradeNo) {

        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setOutTradeNo(outTradeNo);

        GroupBuyOrderList groupBuyOrderListRes = groupBuyOrderListDao.queryGroupBuyOrderRecordByOutTradeNo(groupBuyOrderListReq);

        if (null == groupBuyOrderListRes) {
            log.info("TradeRepository - queryNoPayMarketPayOrderByOutTradeNo - groupBuyOrderListRes is null");
            return null;
        }

        return MarketPayOrderEntity.builder()
                .teamId(groupBuyOrderListRes.getTeamId())
                .orderId(groupBuyOrderListRes.getOrderId())
                .originalPrice(groupBuyOrderListRes.getOriginalPrice())
                .deductionPrice(groupBuyOrderListRes.getDeductionPrice())
                .payPrice(groupBuyOrderListRes.getPayPrice())
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.valueOf(groupBuyOrderListRes.getStatus()))
                .build();
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {

        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.queryGroupBuyProgress(teamId);
        if (null == groupBuyOrder) return null;

        return GroupBuyProgressVO.builder().targetCount(groupBuyOrder.getTargetCount())
                .completeCount(groupBuyOrder.getCompleteCount()).lockCount(groupBuyOrder.getLockCount()).build();

    }

    /**
     * 完整的事务 同时失败或同时成功
     *
     * @param groupBuyOrderAggregate
     * @return
     */
    @Transactional(timeout = 500)
    @Override
    public MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate) {

        //聚合对象信息
        UserEntity userEntity = groupBuyOrderAggregate.getUserEntity();
        PayActivityEntity payActivityEntity = groupBuyOrderAggregate.getPayActivityEntity();
        PayDiscountEntity payDiscountEntity = groupBuyOrderAggregate.getPayDiscountEntity();
        NotifyConfigVO notifyConfigVO = payDiscountEntity.getNotifyConfigVO();
        Integer userTakeOrderCount = groupBuyOrderAggregate.getUserTakeOrderCount();

        // 判断是否有团 - teamId 为空 - 新团、为不空 - 老团
        String teamId = payActivityEntity.getTeamId();
        if (StringUtils.isBlank(teamId)) {
            teamId = RandomStringUtils.randomNumeric(8);

            Date currentTime = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentTime);
            calendar.add(Calendar.MINUTE, payActivityEntity.getValidTime());

            GroupBuyOrder groupBuyOrder = GroupBuyOrder.builder().teamId(teamId)
                    .activityId(payActivityEntity.getActivityId()).source(payDiscountEntity.getSource())
                    .channel(payDiscountEntity.getChannel()).originalPrice(payDiscountEntity.getOriginalPrice())
                    .deductionPrice(payDiscountEntity.getDeductionPrice()).payPrice(payDiscountEntity.getPayPrice())
                    .targetCount(payActivityEntity.getTargetCount()).completeCount(0).lockCount(1)
                    .notifyType(notifyConfigVO.getNotifyType().getCode())
                    .notifyUrl(notifyConfigVO.getNotifyUrl())
                    .validStartTime(currentTime)
                    .validEndTime(calendar.getTime())
                    .build();

            // 写入记录
            groupBuyOrderDao.insert(groupBuyOrder);
        } else {
            // 更新记录 - 如果更新记录不等于1，则表示拼团已满，抛出异常
            int updateAddLockCount = groupBuyOrderDao.updateAddLockCount(teamId);
            if (1 != updateAddLockCount) {
                throw new AppException(ResponseCode.E0005);
            }
        }

        String bizId = payActivityEntity.getActivityId() + Constants.UNDERLINE + userEntity.getUserId() + Constants.UNDERLINE + (userTakeOrderCount + 1);

        // 使用 RandomStringUtils.randomNumeric 替代公司里使用的雪花算法UUID
        String orderId = RandomStringUtils.randomNumeric(12);
        GroupBuyOrderList groupBuyOrderListReq = GroupBuyOrderList.builder().userId(userEntity.getUserId())
                .teamId(teamId).orderId(orderId).activityId(payActivityEntity.getActivityId())
                .startTime(payActivityEntity.getStartTime()).endTime(payActivityEntity.getEndTime())
                .goodsId(payDiscountEntity.getGoodsId()).source(payDiscountEntity.getSource())
                .channel(payDiscountEntity.getChannel()).originalPrice(payDiscountEntity.getOriginalPrice())
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .payPrice(payDiscountEntity.getPayPrice())
                .status(TradeOrderStatusEnumVO.CREATE.getCode())
                .outTradeNo(payDiscountEntity.getOutTradeNo())
                .bizId(bizId)
                .build();

        //解决唯一索引冲突
        try {
            // 写入拼团记录
            groupBuyOrderListDao.insert(groupBuyOrderListReq);
        } catch (DuplicateKeyException e) {
            throw new AppException(ResponseCode.INDEX_EXCEPTION);
        }

        return MarketPayOrderEntity.builder()
                .teamId(teamId)
                .orderId(orderId)
                .originalPrice(payDiscountEntity.getOriginalPrice())
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .payPrice(payDiscountEntity.getPayPrice())
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.CREATE).build();
    }

    @Override
    public GroupBuyActivityEntity queryGroupBuyActivityByActivityId(Long activityId) {
        GroupBuyActivity groupBuyActivity = groupBuyActivityDao.queryGroupBuyActivityByActivityId(activityId);

        return GroupBuyActivityEntity.builder().activityId(groupBuyActivity.getActivityId())
                .activityName(groupBuyActivity.getActivityName()).discountId(groupBuyActivity.getDiscountId())
                .groupType(groupBuyActivity.getGroupType()).takeLimitCount(groupBuyActivity.getTakeLimitCount())
                .target(groupBuyActivity.getTarget()).validTime(groupBuyActivity.getValidTime())
                .status(ActivityStatusEnumVO.valueOf(groupBuyActivity.getStatus()))
                .startTime(groupBuyActivity.getStartTime()).endTime(groupBuyActivity.getEndTime())
                .tagId(groupBuyActivity.getTagId()).tagScope(groupBuyActivity.getTagScope()).build();
    }

    @Override
    public Integer queryOrderCountByActivityId(Long activityId, String userId) {
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setActivityId(activityId);
        groupBuyOrderListReq.setUserId(userId);

        return groupBuyOrderListDao.queryOrderCountByActivityId(groupBuyOrderListReq);
    }

    @Override
    public GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId) {
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.queryGroupBuyTeamByTeamId(teamId);
        return GroupBuyTeamEntity.builder().teamId(groupBuyOrder.getTeamId()).activityId(groupBuyOrder.getActivityId())
                .targetCount(groupBuyOrder.getTargetCount()).completeCount(groupBuyOrder.getCompleteCount())
                .lockCount(groupBuyOrder.getLockCount()).status(GroupBuyOrderEnumVO.valueOf(groupBuyOrder.getStatus()))
                .validStartTime(groupBuyOrder.getValidStartTime()).validEndTime(groupBuyOrder.getValidEndTime())
                .notifyConfigVO(NotifyConfigVO.builder()
                        .notifyType(NotifyTypeEnumVO.valueOf(groupBuyOrder.getNotifyType()))
                        .notifyUrl(groupBuyOrder.getNotifyUrl())
                        // MQ 是固定的
                        .notifyMQ(topic_team_success)
                        .build())
                .build();
    }

    /**
     * 事务性操作
     *
     * @param groupBuyTeamSettlementAggregate
     * @return
     */
    @Transactional(timeout = 500)
    @Override
    public NotifyTaskEntity settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate) {
        // 拆解聚合对象
        UserEntity userEntity = groupBuyTeamSettlementAggregate.getUserEntity();
        GroupBuyTeamEntity groupBuyTeamEntity = groupBuyTeamSettlementAggregate.getGroupBuyTeamEntity();
        NotifyConfigVO notifyConfigVO = groupBuyTeamEntity.getNotifyConfigVO();
        TradePaySuccessEntity tradePaySuccessEntity = groupBuyTeamSettlementAggregate.getTradePaySuccessEntity();

        // 创建入参对象，更新拼团订单明细
        GroupBuyOrderList groupBuyOrderListReq = GroupBuyOrderList.builder()
                .userId(userEntity.getUserId())
                .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                .source(tradePaySuccessEntity.getSource())
                .channel(tradePaySuccessEntity.getChannel())
                .outTradeTime(tradePaySuccessEntity.getOutTradeTime())
                .build();
        int updateOrderListStatusCount = groupBuyOrderListDao.updateOrderStatus2COMPLETE(groupBuyOrderListReq);

        //更新失败 抛出异常
        if (1 != updateOrderListStatusCount) {
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        //更新拼团的进度
        int updateOrderAddCompleteCount = groupBuyOrderDao.updateAddCompleteCount(groupBuyTeamEntity.getTeamId());
        if (1 != updateOrderAddCompleteCount) {
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        //更新到最后一笔时，拼团更新为完成
        if ((groupBuyTeamEntity.getTargetCount() - groupBuyTeamEntity.getCompleteCount()) == 1) {
            int updateOrderStatus = groupBuyOrderDao.updateOrderStatus2COMPLETE(groupBuyTeamEntity.getTeamId());
            if (1 != updateOrderStatus) {
                throw new AppException(ResponseCode.UPDATE_ZERO);
            }

            //查询并返回拼团成功单的外部单号
            List<String> outTradeNoList = groupBuyOrderListDao.queryGroupBuyCompleteOrderOutTradeNoListByTeamId(groupBuyTeamEntity.getTeamId());

            //将外部单号和活动id和拼团id 一起回调
            NotifyTask notifyTask = new NotifyTask();
            notifyTask.setActivityId(groupBuyTeamEntity.getActivityId());
            notifyTask.setTeamId(groupBuyTeamEntity.getTeamId());
            notifyTask.setNotifyCategory(TaskNotifyCategoryEnumVO.TRADE_SETTLEMENT.getCode());
            notifyTask.setNotifyType(notifyConfigVO.getNotifyType().getCode());
            notifyTask.setNotifyMQ(NotifyTypeEnumVO.MQ.equals(notifyConfigVO.getNotifyType()) ? notifyConfigVO.getNotifyMQ() : null);
            notifyTask.setNotifyUrl(NotifyTypeEnumVO.HTTP.equals(notifyConfigVO.getNotifyType()) ? notifyConfigVO.getNotifyUrl() : null);
            notifyTask.setNotifyCount(0);
            notifyTask.setNotifyStatus(0);
            notifyTask.setUuid(groupBuyTeamEntity.getTeamId() + Constants.UNDERLINE +
                    TaskNotifyCategoryEnumVO.TRADE_SETTLEMENT.getCode() + Constants.UNDERLINE +
                    tradePaySuccessEntity.getOutTradeNo());

            notifyTask.setParameterJson(JSON.toJSONString(new HashMap<String, Object>() {{
                put("teamId", groupBuyTeamEntity.getTeamId());
                put("outTradeNoList", outTradeNoList);
            }}));

            notifyTaskDao.insert(notifyTask);
            // 最后一笔订单 回调给小型支付系统开始发货
            log.info("TradeRepository - settlementMarketPayOrder - 最后一笔订单 回调给小型支付系统开始发货");
            return NotifyTaskEntity.builder()
                    .teamId(notifyTask.getTeamId())
                    .notifyType(notifyTask.getNotifyType())
                    .notifyMQ(notifyTask.getNotifyMQ())
                    .notifyUrl(notifyTask.getNotifyUrl())
                    .notifyCount(notifyTask.getNotifyCount())
                    .uuid(notifyTask.getUuid())
                    .parameterJson(notifyTask.getParameterJson())
                    .build();
        }

        return null;
    }

    /**
     * 判定渠道是否处于黑名单中，用dcc服务调用redis缓存的渠道黑名单数据
     *
     * @param source
     * @param channel
     * @return
     */
    @Override
    public boolean isSCBlackIntercept(String source, String channel) {
        return dccService.isScBlacklist(source, channel);
    }

    @Override
    public List<NotifyTaskEntity> queryUnExecutedNotifyTaskList() {
        List<NotifyTask> notifyTaskList = notifyTaskDao.queryUnExecutedNotifyTaskList();
        if (notifyTaskList.isEmpty()) return new ArrayList<>();

        List<NotifyTaskEntity> notifyTaskEntities = new ArrayList<>();
        for (NotifyTask notifyTask : notifyTaskList) {

            NotifyTaskEntity notifyTaskEntity = NotifyTaskEntity.builder()
                    .teamId(notifyTask.getTeamId())
                    .notifyType(notifyTask.getNotifyType())
                    .notifyMQ(notifyTask.getNotifyMQ())
                    .notifyUrl(notifyTask.getNotifyUrl())
                    .notifyCount(notifyTask.getNotifyCount())
                    .parameterJson(notifyTask.getParameterJson())
                    .uuid(notifyTask.getUuid())
                    .build();

            notifyTaskEntities.add(notifyTaskEntity);
        }

        return notifyTaskEntities;
    }

    @Override
    public List<NotifyTaskEntity> queryUnExecutedNotifyTaskList(String teamId) {
        NotifyTask notifyTask = notifyTaskDao.queryUnExecutedNotifyTaskByTeamId(teamId);
        if (notifyTask == null) return new ArrayList<>();

        List<NotifyTaskEntity> notifyTaskEntities = new ArrayList<>();
        NotifyTaskEntity notifyTaskEntity = NotifyTaskEntity.builder()
                .teamId(notifyTask.getTeamId())
                .notifyType(notifyTask.getNotifyType())
                .notifyMQ(notifyTask.getNotifyMQ())
                .notifyUrl(notifyTask.getNotifyUrl())
                .notifyCount(notifyTask.getNotifyCount())
                .parameterJson(notifyTask.getParameterJson())
                .uuid(notifyTask.getUuid())
                .build();
        notifyTaskEntities.add(notifyTaskEntity);
        return notifyTaskEntities;
    }

    /**
     * 占用库存
     * <p>
     * 关于 Redis 独占锁和无锁化设计；<a href="https://bugstack.cn/md/road-map/redis.html">Redis 缓存、加锁(独占/分段)、发布/订阅，常用特性的使用和高级编码操作</a>
     */
    /**
     * 双重独占拼团位置机制
     *
     * @param teamStockKey
     * @param recoveryTeamStockKey
     * @param target
     * @param validTime
     * @return
     */
    @Override
    public boolean occupyTeamStock(String teamStockKey, String recoveryTeamStockKey, Integer target, Integer validTime) {
        //失败恢复量
        Long recoverCount = redisService.getAtomicLong(recoveryTeamStockKey);
        recoverCount = null == recoverCount ? 0L : recoverCount;

        // 1. incr 得到值，与总量和恢复量做对比。恢复量为系统失败时候记录的量。
        // 2. 从有组队量开始，相当于已经有了一个占用量，所以要 +1.(开团的人没有计入teamStockKey)
        long occupy = redisService.incr(teamStockKey) + 1;

        // 当前占用拼团人数多于团的目标数，无法实现拼团，由于刚才做了teamStockKey自增操作，所以需要修正回目标数
        if (occupy > target + recoverCount) {
            redisService.setAtomicLong(teamStockKey, target);
            return false;
        }

        // 1. 给每个产生的值加锁为兜底设计，虽然incr操作是原子的，基本不会产生一样的值。但在实际生产中，遇到过集群的运维配置问题，以及业务运营配置数据问题，导致incr得到的值相同。
        // 2. validTime + 60分钟，是一个延后时间的设计，让数据保留时间稍微长一些，便于排查问题。
        // setNx 只在 key 不存在时才设置值
        String lockKey = teamStockKey + Constants.UNDERLINE + occupy;
        Boolean lock = redisService.setNx(lockKey, validTime + 60, TimeUnit.MINUTES);

        if (!lock) {
            log.info("组队库存加锁失败 {}", lockKey);
        }

        return lock;
    }

    @Override
    public void recoveryTeamStock(String recoveryTeamStockKey, Integer validTime) {
        // 首次组队拼团，是没有 teamId 的，所以不需要这个做处理。
        if (StringUtil.isBlank(recoveryTeamStockKey)) {
            return;
        }

        // 增加恢复占用量
        redisService.incr(recoveryTeamStockKey);
    }

    @Override
    @Transactional(timeout = 5000)
    public NotifyTaskEntity unpaid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate) {
        TradeRefundOrderEntity tradeRefundOrderEntity = groupBuyRefundAggregate.getTradeRefundOrderEntity();
        GroupBuyProgressVO groupBuyProgress = groupBuyRefundAggregate.getGroupBuyProgress();

        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        // 保留userId，企业中往往会根据 userId 作为分库分表路由键，如果将来做分库分表也可以方便处理
        groupBuyOrderListReq.setUserId(tradeRefundOrderEntity.getUserId());
        groupBuyOrderListReq.setOrderId(tradeRefundOrderEntity.getOrderId());

        int updateUnpaid2RefundCount = groupBuyOrderListDao.unpaid2Refund(groupBuyOrderListReq);
        if (1 != updateUnpaid2RefundCount) {
            log.error("逆向流程-unpaid2Refund，更新订单状态(退单)失败 {} {}", tradeRefundOrderEntity.getUserId(), tradeRefundOrderEntity.getOrderId());
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        GroupBuyOrder groupBuyOrderReq = new GroupBuyOrder();
        groupBuyOrderReq.setTeamId(tradeRefundOrderEntity.getTeamId());
        groupBuyOrderReq.setLockCount(groupBuyProgress.getLockCount());

        int updateTeamUnpaid2Refund = groupBuyOrderDao.unpaid2Refund(groupBuyOrderReq);
        if (1 != updateTeamUnpaid2Refund) {
            log.error("逆向流程-unpaid2Refund，更新组队记录(退单)失败 {} {}", tradeRefundOrderEntity.getUserId(), tradeRefundOrderEntity.getOrderId());
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        // 逆向后，还要处理 redis recoveryCount 恢复了，这部分最后统一处理

        NotifyTask notifyTask = new NotifyTask();
        notifyTask.setTeamId(tradeRefundOrderEntity.getTeamId());
        notifyTask.setActivityId(tradeRefundOrderEntity.getActivityId());
        notifyTask.setNotifyCategory(TaskNotifyCategoryEnumVO.TRADE_UNPAID2REFUND.getCode());
        notifyTask.setNotifyType(NotifyTypeEnumVO.MQ.getCode());
        notifyTask.setNotifyMQ(topic_team_refund);
        notifyTask.setNotifyCount(0);
        notifyTask.setNotifyStatus(0);
        notifyTask.setUuid(tradeRefundOrderEntity.getTeamId() + Constants.UNDERLINE
                + TaskNotifyCategoryEnumVO.TRADE_UNPAID2REFUND.getCode() + Constants.UNDERLINE
                + tradeRefundOrderEntity.getOrderId());
        notifyTask.setParameterJson(JSON.toJSONString(new HashMap<String, Object>() {{
            put("type", RefundTypeEnumVO.UNPAID_UNLOCK.getCode());
            put("userId", tradeRefundOrderEntity.getUserId());
            put("teamId", tradeRefundOrderEntity.getTeamId());
            put("orderId", tradeRefundOrderEntity.getOrderId());
            put("activityId", tradeRefundOrderEntity.getActivityId());
            put("outTradeNo", tradeRefundOrderEntity.getOutTradeNo());
        }}));

        notifyTaskDao.insert(notifyTask);

        return NotifyTaskEntity.builder()
                .teamId(notifyTask.getTeamId())
                .notifyType(notifyTask.getNotifyType())
                .notifyMQ(notifyTask.getNotifyMQ())
                .notifyCount(notifyTask.getNotifyCount())
                .parameterJson(notifyTask.getParameterJson())
                .uuid(notifyTask.getUuid())
                .build();
    }

    @Override
    @Transactional(timeout = 5000)
    public NotifyTaskEntity paid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate) {
        GroupBuyProgressVO groupBuyProgress = groupBuyRefundAggregate.getGroupBuyProgress();
        TradeRefundOrderEntity tradeRefundOrderEntity = groupBuyRefundAggregate.getTradeRefundOrderEntity();

        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(tradeRefundOrderEntity.getUserId());
        groupBuyOrderListReq.setOrderId(tradeRefundOrderEntity.getOrderId());

        int updatePaid2RefundCount = groupBuyOrderListDao.paid2Refund(groupBuyOrderListReq);
        if (1 != updatePaid2RefundCount) {
            log.error("逆向流程-paid2Refund，更新订单状态(退单)失败 {} {}", tradeRefundOrderEntity.getUserId(), tradeRefundOrderEntity.getOrderId());
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        GroupBuyOrder groupBuyOrderReq = new GroupBuyOrder();
        groupBuyOrderReq.setTeamId(tradeRefundOrderEntity.getTeamId());
        groupBuyOrderReq.setLockCount(groupBuyProgress.getLockCount());
        groupBuyOrderReq.setCompleteCount(groupBuyProgress.getCompleteCount());
        int updateTeamPaid2RefundCount = groupBuyOrderDao.paid2Refund(groupBuyOrderReq);
        if (1 != updateTeamPaid2RefundCount) {
            log.error("逆向流程-paid2Refund，更新组队记录(退单)失败 {} {}", tradeRefundOrderEntity.getUserId(), tradeRefundOrderEntity.getOrderId());
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        NotifyTask notifyTask = new NotifyTask();
        notifyTask.setActivityId(tradeRefundOrderEntity.getActivityId());
        notifyTask.setTeamId(tradeRefundOrderEntity.getTeamId());
        notifyTask.setNotifyCategory(TaskNotifyCategoryEnumVO.TRADE_PAID2REFUND.getCode());
        notifyTask.setNotifyType(NotifyTypeEnumVO.MQ.getCode());
        notifyTask.setNotifyMQ(topic_team_refund);
        notifyTask.setNotifyCount(0);
        notifyTask.setNotifyStatus(0);
        notifyTask.setUuid(tradeRefundOrderEntity.getTeamId() + Constants.UNDERLINE
                + TaskNotifyCategoryEnumVO.TRADE_PAID2REFUND.getCode() + Constants.UNDERLINE
                + tradeRefundOrderEntity.getOrderId());
        notifyTask.setParameterJson(JSON.toJSONString(
                new HashMap<String, Object>() {{
                    put("type", RefundTypeEnumVO.PAID_UNFORMED.getCode());
                    put("userId", tradeRefundOrderEntity.getUserId());
                    put("teamId", tradeRefundOrderEntity.getTeamId());
                    put("orderId", tradeRefundOrderEntity.getOrderId());
                    put("activityId", tradeRefundOrderEntity.getActivityId());
                    put("outTradeNo", tradeRefundOrderEntity.getOutTradeNo());
                }}
        ));

        notifyTaskDao.insert(notifyTask);

        // 返回给service 先单发一次MQ
        return NotifyTaskEntity.builder()
                .teamId(tradeRefundOrderEntity.getTeamId())
                .notifyType(notifyTask.getNotifyType())
                .notifyMQ(notifyTask.getNotifyMQ())
                .notifyCount(notifyTask.getNotifyCount())
                .parameterJson(notifyTask.getParameterJson())
                .uuid(notifyTask.getUuid())
                .build();
    }

    @Override
    @Transactional(timeout = 5000)
    public NotifyTaskEntity paidTeam2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate) {
        TradeRefundOrderEntity tradeRefundOrderEntity = groupBuyRefundAggregate.getTradeRefundOrderEntity();
        GroupBuyProgressVO groupBuyProgress = groupBuyRefundAggregate.getGroupBuyProgress();
        GroupBuyOrderEnumVO groupBuyOrderEnumVO = groupBuyRefundAggregate.getGroupBuyOrderEnumVO();

        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(tradeRefundOrderEntity.getUserId());
        groupBuyOrderListReq.setOrderId(tradeRefundOrderEntity.getOrderId());

        //1.更新订单明细
        int updatePaidTeam2RefundCount = groupBuyOrderListDao.paidTeam2Refund(groupBuyOrderListReq);

        if (1 != updatePaidTeam2RefundCount) {
            log.error("逆向流程-paidTeam2Refund，更新订单状态(退单)失败 {} {}", tradeRefundOrderEntity.getUserId(), tradeRefundOrderEntity.getOrderId());
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        if (GroupBuyOrderEnumVO.FAIL.equals(groupBuyOrderEnumVO)) {
            int updateTeamPaidTeam2RefundFail = groupBuyOrderDao.paidTeam2RefundFail(GroupBuyOrder.builder()
                    .teamId(tradeRefundOrderEntity.getTeamId())
                    .activityId(tradeRefundOrderEntity.getActivityId())
                    .lockCount(groupBuyProgress.getLockCount())
                    .completeCount(groupBuyProgress.getCompleteCount())
                    .build());

            if (1 != updateTeamPaidTeam2RefundFail) {
                log.error("逆向流程-paidTeam2RefundFail，更新订单状态(退单)失败 {} {}", tradeRefundOrderEntity.getUserId(), tradeRefundOrderEntity.getOrderId());
                throw new AppException(ResponseCode.UPDATE_ZERO);
            }
        } else if (GroupBuyOrderEnumVO.COMPLETE_FAIL.equals(groupBuyOrderEnumVO)) {
            int updateTeamPaidTeam2Refund = groupBuyOrderDao.paidTeam2Refund(GroupBuyOrder.builder()
                    .teamId(tradeRefundOrderEntity.getTeamId())
                    .activityId(tradeRefundOrderEntity.getActivityId())
                    .lockCount(groupBuyProgress.getLockCount())
                    .completeCount(groupBuyProgress.getCompleteCount())
                    .build());
            if (1 != updateTeamPaidTeam2Refund) {
                log.error("逆向流程-paidTeam2Refund，更新订单状态(退单)失败 {} {}", tradeRefundOrderEntity.getUserId(), tradeRefundOrderEntity.getOrderId());
                throw new AppException(ResponseCode.UPDATE_ZERO);
            }
        }

        NotifyTask notifyTask = new NotifyTask();
        notifyTask.setActivityId(tradeRefundOrderEntity.getActivityId());
        notifyTask.setTeamId(tradeRefundOrderEntity.getTeamId());
        notifyTask.setNotifyCategory(TaskNotifyCategoryEnumVO.TRADE_PAID_TEAM2REFUND.getCode());
        notifyTask.setNotifyType(NotifyTypeEnumVO.MQ.getCode());
        notifyTask.setNotifyMQ(topic_team_refund);
        notifyTask.setNotifyCount(0);
        notifyTask.setNotifyStatus(0);
        notifyTask.setUuid(
                tradeRefundOrderEntity.getTeamId() + Constants.UNDERLINE +
                        TaskNotifyCategoryEnumVO.TRADE_PAID_TEAM2REFUND.getCode() + Constants.UNDERLINE +
                        tradeRefundOrderEntity.getOrderId());

        notifyTask.setParameterJson(JSON.toJSONString(
                new HashMap<String, Object>() {{
                    put("type", RefundTypeEnumVO.PAID_FORMED.getCode());
                    put("userId", tradeRefundOrderEntity.getUserId());
                    put("teamId", tradeRefundOrderEntity.getTeamId());
                    put("orderId", tradeRefundOrderEntity.getOrderId());
                    put("activityId", tradeRefundOrderEntity.getActivityId());
                    put("outTradeNo", tradeRefundOrderEntity.getOutTradeNo());
                }}
        ));
        notifyTaskDao.insert(notifyTask);

        return NotifyTaskEntity.builder()
                .teamId(tradeRefundOrderEntity.getTeamId())
                .notifyType(notifyTask.getNotifyType())
                .notifyMQ(notifyTask.getNotifyMQ())
                .notifyCount(notifyTask.getNotifyCount())
                .parameterJson(notifyTask.getParameterJson())
                .uuid(notifyTask.getUuid())
                .build();
    }

    @Override
    public void refund2AddRecovery(String recoveryTeamStockKey, String orderId) throws Exception {
        // 如果恢复库存key为空，直接返回
        if (StringUtils.isBlank(recoveryTeamStockKey) || StringUtils.isBlank(orderId)) {
            return;
        }

        // 使用orderId作为锁的key，避免同一订单重复恢复库存
        String lockKey = "refund_lock_" + orderId;

        // 尝试获取分布式锁，防止重复操作 30天过期
        Boolean lockAcquired = redisService.setNx(lockKey, 30 * 24 * 60 * 60 * 1000L, TimeUnit.MINUTES);

        if (!lockAcquired) {
            log.warn("订单 {} 恢复库存操作已在进行中，跳过重复操作", orderId);
            return;
        }

        // 这种设计保证了同一次恢复库存任务不会重复执行，但是对于同订单的后续恢复库存任务则无法执行，也就是同一订单的库存只能恢复一次
        try {
            // 在锁保护下执行库存恢复操作
            redisService.incr(recoveryTeamStockKey);
            log.info("订单 {} 恢复库存成功，恢复库存key: {}", orderId, recoveryTeamStockKey);
        } catch (Exception e) {
            log.error("订单 {} 恢复库存失败，恢复库存key: {}", orderId, recoveryTeamStockKey, e);
            // 如果抛异常则释放锁，允许MQ重新消费恢复库存
            redisService.remove(lockKey);
            throw e;
        }
    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> queryTimeoutUnpaidOrderList() {
        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.queryTimeoutUnpaidOrderList();
        if (null == groupBuyOrderLists || groupBuyOrderLists.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有teamId
        Set<String> teamIds = groupBuyOrderLists.stream()
                .map(GroupBuyOrderList::getTeamId)
                .collect(Collectors.toSet());

        // 查询团队信息
        List<GroupBuyOrder> groupBuyOrders = groupBuyOrderDao.queryGroupBuyTeamByTeamIds(teamIds);
        if (null == groupBuyOrders || groupBuyOrders.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, GroupBuyOrder> groupBuyOrderMap = groupBuyOrders.stream()
                .collect(Collectors.toMap(GroupBuyOrder::getTeamId, order -> order));

        // 转换数据
        List<UserGroupBuyOrderDetailEntity> userGroupBuyOrderDetailEntities = new ArrayList<>();
        for (GroupBuyOrderList groupBuyOrderList : groupBuyOrderLists) {
            String teamId = groupBuyOrderList.getTeamId();
            GroupBuyOrder groupBuyOrder = groupBuyOrderMap.get(teamId);
            if (null == groupBuyOrder) continue;

            UserGroupBuyOrderDetailEntity userGroupBuyOrderDetailEntity = UserGroupBuyOrderDetailEntity.builder()
                    .userId(groupBuyOrderList.getUserId())
                    .teamId(groupBuyOrder.getTeamId())
                    .activityId(groupBuyOrder.getActivityId())
                    .targetCount(groupBuyOrder.getTargetCount())
                    .completeCount(groupBuyOrder.getCompleteCount())
                    .lockCount(groupBuyOrder.getLockCount())
                    .validStartTime(groupBuyOrder.getValidStartTime())
                    .validEndTime(groupBuyOrder.getValidEndTime())
                    .outTradeNo(groupBuyOrderList.getOutTradeNo())
                    .source(groupBuyOrderList.getSource())
                    .channel(groupBuyOrderList.getChannel())
                    .build();

            userGroupBuyOrderDetailEntities.add(userGroupBuyOrderDetailEntity);
        }

        return userGroupBuyOrderDetailEntities;
    }

    @Override
    public int updateNotifyTaskStatusSuccess(NotifyTaskEntity notifyTaskEntity) {
        NotifyTask notifyTask = new NotifyTask();
        notifyTask.setTeamId(notifyTaskEntity.getTeamId());
        notifyTask.setUuid(notifyTaskEntity.getUuid());

        return notifyTaskDao.updateNotifyTaskStatusSuccess(notifyTask);
    }

    @Override
    public int updateNotifyTaskStatusError(NotifyTaskEntity notifyTaskEntity) {
        NotifyTask notifyTask = new NotifyTask();
        notifyTask.setTeamId(notifyTaskEntity.getTeamId());
        notifyTask.setUuid(notifyTaskEntity.getUuid());
        return notifyTaskDao.updateNotifyTaskStatusError(notifyTask);
    }

    @Override
    public int updateNotifyTaskStatusRetry(NotifyTaskEntity notifyTaskEntity) {
        NotifyTask notifyTask = new NotifyTask();
        notifyTask.setTeamId(notifyTaskEntity.getTeamId());
        notifyTask.setUuid(notifyTaskEntity.getUuid());
        return notifyTaskDao.updateNotifyTaskStatusRetry(notifyTask);
    }


}
