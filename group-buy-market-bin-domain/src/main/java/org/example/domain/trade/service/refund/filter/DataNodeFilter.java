package org.example.domain.trade.service.refund.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.trade.adapter.repository.ITradeRepository;
import org.example.domain.trade.model.entity.GroupBuyTeamEntity;
import org.example.domain.trade.model.entity.MarketPayOrderEntity;
import org.example.domain.trade.model.entity.TradeRefundBehaviorEntity;
import org.example.domain.trade.model.entity.TradeRefundCommandEntity;
import org.example.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import org.example.domain.trade.service.refund.factory.TradeRefundRuleFilterFactory;
import org.example.types.enums.GroupBuyOrderEnumVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassName : DataNodeFilter
 * @Description : 数据节点
 * @Author : Bingo
 * @Date: 2026/2/26  14:17
 */
@Service
@Slf4j
public class DataNodeFilter implements ILogicHandler<TradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> {

    @Resource
    private ITradeRepository repository;

    @Override
    public TradeRefundBehaviorEntity apply(TradeRefundCommandEntity tradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext dynamicContext) throws Exception {

        // 1. 查询外部交易单，组队id、orderId、拼团状态
        MarketPayOrderEntity marketPayOrderEntity = repository.queryMarketPayOrderByOutTradeNo(tradeRefundCommandEntity.getUserId(), tradeRefundCommandEntity.getOutTradeNo());
        TradeOrderStatusEnumVO tradeOrderStatusEnumVO = marketPayOrderEntity.getTradeOrderStatusEnumVO();

        // 2. 查询拼团状态
        GroupBuyTeamEntity groupBuyTeamEntity = repository.queryGroupBuyTeamByTeamId(marketPayOrderEntity.getTeamId());
        GroupBuyOrderEnumVO groupBuyOrderEnumVO = groupBuyTeamEntity.getStatus();

        // 一次性查询完所有数据，传给后续需要使用的节点，因为数据查询可能涉及到多线程，所以放在一起查询。
        dynamicContext.setMarketPayOrderEntity(marketPayOrderEntity);
        dynamicContext.setGroupBuyTeamEntity(groupBuyTeamEntity);

        return next(tradeRefundCommandEntity, dynamicContext);
    }
}
