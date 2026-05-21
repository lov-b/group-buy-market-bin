package org.example.infrastructure.adapter.repository;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.activity.adapter.repository.IActivityRepository;
import org.example.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import org.example.domain.activity.model.valobj.*;
import org.example.infrastructure.dao.*;
import org.example.infrastructure.dao.po.*;
import org.example.infrastructure.dcc.DCCService;
import org.example.infrastructure.redis.IRedisService;
import org.redisson.api.RBitSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName : ActivityRepository
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/25  15:30
 */
@Slf4j
@Repository
public class ActivityRepository extends AbstractRepository implements IActivityRepository {

    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;
    @Resource
    private IGroupBuyDiscountDao groupBuyDiscountDao;
    @Resource
    private ISkuDao skuDao;
    @Resource
    private ISCSkuActivityDao iscSkuActivityDao;
    @Resource
    private IRedisService redisService;
    @Resource
    private DCCService dccService;
    @Resource
    private IGroupBuyOrderDao groupBuyOrderDao;
    @Resource
    private IGroupBuyOrderListDao groupBuyOrderListDao;

    @Override
    public GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(Long activityId) {

        // 获取redis key 用于后续redis查询
        String groupBuyActivityCacheKey = GroupBuyActivity.cacheRedisKey(activityId);
        // 传入redis key和dao数据库查询函数
        GroupBuyActivity groupBuyActivityRes = getFromRedisOrDB(
                groupBuyActivityCacheKey,
                () -> groupBuyActivityDao.queryValidGroupBuyActivityId(activityId));


        String discountId = groupBuyActivityRes.getDiscountId();
        //和上述同理
        String groupBuyDiscountKey = GroupBuyDiscount.cacheRedisKey(discountId);
        GroupBuyDiscount groupBuyDiscountRes = getFromRedisOrDB(
                groupBuyDiscountKey,
                () -> groupBuyDiscountDao.queryGroupBuyActivityDiscountByDiscountId(discountId),
                60);


        GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount = GroupBuyActivityDiscountVO.GroupBuyDiscount.builder()
                .discountName(groupBuyDiscountRes.getDiscountName())
                .discountDesc(groupBuyDiscountRes.getDiscountDesc())
                .discountType(DiscountTypeEnum.get(groupBuyDiscountRes.getDiscountType()))
                .marketPlan(groupBuyDiscountRes.getMarketPlan())
                .marketExpr(groupBuyDiscountRes.getMarketExpr())
                .tagId(groupBuyDiscountRes.getTagId())
                .build();

        return GroupBuyActivityDiscountVO.builder()
                .activityId(groupBuyActivityRes.getActivityId())
                .activityName(groupBuyActivityRes.getActivityName())
                .groupBuyDiscount(groupBuyDiscount)
                .groupType(groupBuyActivityRes.getGroupType())
                .takeLimitCount(groupBuyActivityRes.getTakeLimitCount())
                .target(groupBuyActivityRes.getTarget())
                .validTime(groupBuyActivityRes.getValidTime())
                .status(groupBuyActivityRes.getStatus())
                .startTime(groupBuyActivityRes.getStartTime())
                .endTime(groupBuyActivityRes.getEndTime())
                .tagId(groupBuyActivityRes.getTagId())
                .tagScope(groupBuyActivityRes.getTagScope())
                .build();
    }

    @Override
    public SkuVO querySkuByGoodsId(String goodsId) {
        Sku sku = skuDao.querySkuByGoodsId(goodsId);
        if (null == sku) return null;
        return SkuVO.builder()
                .goodsId(sku.getGoodsId())
                .goodsName(sku.getGoodsName())
                .originalPrice(sku.getOriginalPrice())
                .build();
    }

    @Override
    public SCSkuActivityVO querySCSkuActivityBySCGoodsId(String source, String channel, String goodsId) {

        SCSkuActivity scSkuActivityReq = new SCSkuActivity();
        scSkuActivityReq.setGoodsId(goodsId);
        scSkuActivityReq.setChannel(channel);
        scSkuActivityReq.setSource(source);
        SCSkuActivity scSkuActivity = iscSkuActivityDao.querySCSkuActivityBySCGoodsId(scSkuActivityReq);
        if (null == scSkuActivity) return null;

        return SCSkuActivityVO.builder()
                .source(scSkuActivity.getSource())
                .channel(scSkuActivity.getChannel())
                .activityId(scSkuActivity.getActivityId())
                .goodsId(scSkuActivity.getGoodsId())
                .build();
    }

    @Override
    public boolean isTagCrowdRange(String tagId, String userId) {

        //获取redis中保存的bit map的人群
        RBitSet bitSet = redisService.getBitSet(tagId);

        //核实返回的bitSet是否存在 不存在说明没有该条人群标签信息 直接放行拼团展示和参与
        if (!bitSet.isExists()) return true;

        //返回当前用户在当前人群标签中的数值 false表示不在标签中 ture表示在标签中
        return bitSet.get(redisService.getIndexFromUserId(userId));
    }

    @Override
    public boolean downgradeSwitch() {
        return dccService.isDowngradeSwitch();
    }

    @Override
    public boolean cutRange(String userId) {
        return dccService.isCutRange(userId);
    }

    /**
     * 查询拼团明细 - 通过用户id
     *
     * @param activityId
     * @param userId
     * @param ownerCount
     * @return
     */
    @Override
    public List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailListByOwner(Long activityId, String userId, Integer ownerCount) {
        //1.查询用户所在的拼团队伍信息 - 拼团明细表
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setActivityId(activityId);
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setCount(ownerCount);

        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.queryInProgressUserGroupBuyOrderDetailListByUserId(groupBuyOrderListReq);
        if (null == groupBuyOrderLists || groupBuyOrderLists.isEmpty()) {
            log.info("ActivityRepository - queryInProgressUserGroupBuyOrderDetailListByOwner: null == groupBuyOrderLists || groupBuyOrderLists.isEmpty()");
            return null;
        }

        //2.过滤出team Id - 从 groupBuyOrderLists 中抽取所有有效 teamId，过滤掉 null/空字符串，并去重得到一个 Set
        Set<String> teamIds = groupBuyOrderLists.stream()
                .map(GroupBuyOrderList::getTeamId)
                .filter(teamId -> teamId != null && !teamId.isEmpty())
                .collect(Collectors.toSet());

        // 3. 查询队伍明细，组装Map结构
        List<GroupBuyOrder> groupBuyOrders = groupBuyOrderDao.queryGroupBuyProgressByTeamIds(teamIds);
        if (null == groupBuyOrders || groupBuyOrders.isEmpty()) return null;

        //构建team map
        Map<String, GroupBuyOrder> groupBuyOrderMap = groupBuyOrders.stream()
                .collect(Collectors.toMap(GroupBuyOrder::getTeamId, groupBuyOrder -> groupBuyOrder));

        //4.转换数据 - 因为返回实体中含有 userid, outTradeNO等字段，需要从最开始的 groupBuyOrderLists中获取，所以对其进行遍历
        List<UserGroupBuyOrderDetailEntity> userGroupBuyOrderDetailEntities = new ArrayList<>();
        for (GroupBuyOrderList groupBuyOrderList : groupBuyOrderLists) {
            String teamId = groupBuyOrderList.getTeamId();
            //从 groupBuyOrderMap中获取活动信息
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
                    .build();

            userGroupBuyOrderDetailEntities.add(userGroupBuyOrderDetailEntity);
        }
        return userGroupBuyOrderDetailEntities;
    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailListByRandom(Long activityId, String userId, Integer randomCount) {
        // 1. 根据用户ID、活动ID，查询用户参与的拼团队伍 (user id用于排除当前用户的拼团明细)
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setActivityId(activityId);
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setCount(randomCount * 2);

        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.queryInProgressUserGroupBuyOrderDetailListByRandom(groupBuyOrderListReq);
        log.info("ActivityRepository - queryInProgressUserGroupBuyOrderDetailListByRandom: after function");
        log.info("List<GroupBuyOrderList> groupBuyOrderLists:{}", groupBuyOrderLists);
        if (null == groupBuyOrderLists || groupBuyOrderLists.isEmpty()) {
            log.info("ActivityRepository - queryInProgressUserGroupBuyOrderDetailListByRandom: null == groupBuyOrderLists || groupBuyOrderLists.isEmpty()");
            return null;
        }

        // 判断总量是否大于 randomCount
        if (groupBuyOrderLists.size() > randomCount) {
            // 随机打乱列表
            Collections.shuffle(groupBuyOrderLists);
            // 获取前 randomCount 个元素
            groupBuyOrderLists = groupBuyOrderLists.subList(0, randomCount);
        }

        // 2. 过滤队伍获取 TeamId
        Set<String> teamIds = groupBuyOrderLists.stream()
                .map(GroupBuyOrderList::getTeamId)
                .filter(teamId -> teamId != null && !teamId.isEmpty())
                .collect(Collectors.toSet());

        // 3. 查询队伍明细，组装Map结构
        List<GroupBuyOrder> groupBuyOrders = groupBuyOrderDao.queryGroupBuyProgressByTeamIds(teamIds);
        //构建team map
        Map<String, GroupBuyOrder> groupBuyOrderMap = groupBuyOrders.stream()
                .collect(Collectors.toMap(GroupBuyOrder::getTeamId, groupBuyOrder -> groupBuyOrder));

        //4.转换数据 - 因为返回实体中含有 userid, outTradeNO等字段，需要从最开始的 groupBuyOrderLists中获取，所以对其进行遍历
        List<UserGroupBuyOrderDetailEntity> userGroupBuyOrderDetailEntities = new ArrayList<>();
        for (GroupBuyOrderList groupBuyOrderList : groupBuyOrderLists) {
            String teamId = groupBuyOrderList.getTeamId();
            //从 groupBuyOrderMap中获取活动信息
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
                    .build();
            userGroupBuyOrderDetailEntities.add(userGroupBuyOrderDetailEntity);
        }
        return userGroupBuyOrderDetailEntities;
    }

    @Override
    public TeamStatisticVO queryTeamStatisticByActivityId(Long activityId) {
        // 1. 根据活动ID查询拼团队伍
        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.queryInProgressUserGroupBuyOrderDetailListByActivityId(activityId);

        if (null == groupBuyOrderLists || groupBuyOrderLists.isEmpty()) return new TeamStatisticVO(0, 0, 0);

        // 2. 过滤队伍获取 TeamId
        Set<String> teamIds = groupBuyOrderLists.stream()
                .map(GroupBuyOrderList::getTeamId)
                .filter(teamId -> teamId != null && !teamId.isEmpty())
                .collect(Collectors.toSet());

        //3.统计数据
        Integer allTeamCount = groupBuyOrderDao.queryAllTeamCount(teamIds);
        Integer allTeamCompleteCount = groupBuyOrderDao.queryAllTeamCompleteCount(teamIds);
        Integer allTeamUserCount = groupBuyOrderDao.queryAllUserCount(teamIds);

        //4.构建对象
        return TeamStatisticVO.builder()
                .allTeamCount(allTeamCount)
                .allTeamCompleteCount(allTeamCompleteCount)
                .allTeamUserCount(allTeamUserCount)
                .build();
    }
}
