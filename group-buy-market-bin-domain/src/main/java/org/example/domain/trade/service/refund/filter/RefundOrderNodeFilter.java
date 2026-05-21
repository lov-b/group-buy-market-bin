package org.example.domain.trade.service.refund.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.trade.adapter.repository.ITradeRepository;
import org.example.domain.trade.model.entity.*;
import org.example.domain.trade.model.valobj.RefundTypeEnumVO;
import org.example.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import org.example.domain.trade.service.refund.business.IRefundOrderStrategy;
import org.example.domain.trade.service.refund.factory.TradeRefundRuleFilterFactory;
import org.example.types.enums.GroupBuyOrderEnumVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @ClassName : RefundOrderNodeFilter
 * @Description : 退单节点
 * @Author : Bingo
 * @Date: 2026/2/26  14:17
 */
@Service
@Slf4j
public class RefundOrderNodeFilter implements ILogicHandler<TradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> {

    @Resource
    private ITradeRepository tradeRepository;

    @Resource
    private Map<String, IRefundOrderStrategy> refundOrderStrategyMap;


    @Override
    public TradeRefundBehaviorEntity apply(TradeRefundCommandEntity tradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext dynamicContext) throws Exception {

        // 获取拼团状态
        GroupBuyTeamEntity groupBuyTeamEntity = dynamicContext.getGroupBuyTeamEntity();
        GroupBuyOrderEnumVO groupBuyOrderEnumVO = groupBuyTeamEntity.getStatus();

        // 获取订单状态
        MarketPayOrderEntity marketPayOrderEntity = dynamicContext.getMarketPayOrderEntity();
        TradeOrderStatusEnumVO tradeOrderStatusEnumVO = marketPayOrderEntity.getTradeOrderStatusEnumVO();

        // 获取策略
        RefundTypeEnumVO refundStrategy = RefundTypeEnumVO.getRefundStrategy(groupBuyOrderEnumVO, tradeOrderStatusEnumVO);
        IRefundOrderStrategy iRefundOrderStrategy = refundOrderStrategyMap.get(refundStrategy.getStrategy());

        // 执行退单
        TradeRefundOrderEntity tradeRefundOrderEntity = new TradeRefundOrderEntity();
        tradeRefundOrderEntity.setOrderId(marketPayOrderEntity.getOrderId());
        tradeRefundOrderEntity.setActivityId(groupBuyTeamEntity.getActivityId());
        tradeRefundOrderEntity.setUserId(tradeRefundCommandEntity.getUserId());
        tradeRefundOrderEntity.setTeamId(groupBuyTeamEntity.getTeamId());
        tradeRefundOrderEntity.setOutTradeNo(tradeRefundCommandEntity.getOutTradeNo());
        iRefundOrderStrategy.refundOrder(tradeRefundOrderEntity);

        return TradeRefundBehaviorEntity.builder()
                .userId(tradeRefundCommandEntity.getUserId())
                .teamId(groupBuyTeamEntity.getTeamId())
                .orderId(marketPayOrderEntity.getOrderId())
                .tradeRefundBehaviorEnum(TradeRefundBehaviorEntity.TradeRefundBehaviorEnum.SUCCESS)
                .build();
    }
}
