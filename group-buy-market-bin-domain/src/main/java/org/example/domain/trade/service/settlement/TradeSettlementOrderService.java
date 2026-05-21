package org.example.domain.trade.service.settlement;

import cn.bugstack.wrench.design.framework.link.model2.chain.BusinessLinkedList;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.trade.adapter.port.ITradePort;
import org.example.domain.trade.adapter.repository.ITradeRepository;
import org.example.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import org.example.domain.trade.model.entity.*;
import org.example.domain.trade.service.ITradeSettlementOrderService;
import org.example.domain.trade.service.ITradeTaskService;
import org.example.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import org.example.types.enums.NotifyTaskHTTPEnumVO;
import org.example.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName : TradeSettlementOrderService
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/7  21:27
 */
@Slf4j
@Service
public class TradeSettlementOrderService implements ITradeSettlementOrderService {

    @Resource
    private ITradeRepository repository;
    @Resource
    private ITradePort port;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private ITradeTaskService tradeTaskService;

    @Resource
    private BusinessLinkedList<TradeSettlementRuleCommandEntity,
            TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> tradeSettlementRuleFilter;

    @Override
    public TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception {

        // 1.查询拼团信息
        TradeSettlementRuleCommandEntity tradeSettlementRuleCommandEntity = TradeSettlementRuleCommandEntity.builder()
                .source(tradePaySuccessEntity.getSource())
                .channel(tradePaySuccessEntity.getChannel())
                .userId(tradePaySuccessEntity.getUserId())
                .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                .outTradeTime(tradePaySuccessEntity.getOutTradeTime())
                .build();

        TradeSettlementRuleFilterBackEntity tradeSettlementRuleFilterBackEntity = tradeSettlementRuleFilter.apply(
                tradeSettlementRuleCommandEntity, new TradeSettlementRuleFilterFactory.DynamicContext());

        // 2. 查询组团信息
        GroupBuyTeamEntity groupBuyTeamEntity = GroupBuyTeamEntity.builder()
                .teamId(tradeSettlementRuleFilterBackEntity.getTeamId())
                .activityId(tradeSettlementRuleFilterBackEntity.getActivityId())
                .targetCount(tradeSettlementRuleFilterBackEntity.getTargetCount())
                .completeCount(tradeSettlementRuleFilterBackEntity.getCompleteCount())
                .lockCount(tradeSettlementRuleFilterBackEntity.getLockCount())
                .status(tradeSettlementRuleFilterBackEntity.getStatus())
                .validStartTime(tradeSettlementRuleFilterBackEntity.getValidStartTime())
                .validEndTime(tradeSettlementRuleFilterBackEntity.getValidEndTime())
                .notifyConfigVO(tradeSettlementRuleFilterBackEntity.getNotifyConfigVO())
                .build();

        //3.构建聚合对象
        GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate = GroupBuyTeamSettlementAggregate.builder()
                .groupBuyTeamEntity(groupBuyTeamEntity)
                .tradePaySuccessEntity(tradePaySuccessEntity)
                .userEntity(UserEntity.builder().userId(tradePaySuccessEntity.getUserId()).build())
                .build();

        //4.拼团交易结算
        NotifyTaskEntity notifyTaskEntity = repository.settlementMarketPayOrder(groupBuyTeamSettlementAggregate);
        log.info("notifyTaskEntity:{}", JSON.toJSONString(notifyTaskEntity));

        // 5. 组队回调处理 - 处理失败也会有定时任务补偿，通过这样的方式，可以减轻任务调度，提高时效性
        if (null != notifyTaskEntity) {
            log.info("null != notifyTaskEntity,notifyTaskEntity:{}", JSON.toJSONString(notifyTaskEntity));
            threadPoolExecutor.execute(() -> {
                Map<String, Integer> notifyResultMap = null;
                try {
                    notifyResultMap = tradeTaskService.execNotifyJob(notifyTaskEntity);
                    log.info("回调通知拼团完结 result:{}", JSON.toJSONString(notifyResultMap));
                } catch (Exception e) {
                    log.error("回调通知拼团完结失败 result:{}", JSON.toJSONString(notifyResultMap), e);
                    throw new AppException(e.getMessage());
                }
            });
        }


        //6.返回结算信息
        return TradePaySettlementEntity.builder()
                .source(tradePaySuccessEntity.getSource())
                .channel(tradePaySuccessEntity.getChannel())
                .userId(tradePaySuccessEntity.getUserId())
                .teamId(tradeSettlementRuleFilterBackEntity.getTeamId())
                .activityId(groupBuyTeamEntity.getActivityId())
                .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                .build();
    }

}
