package org.example.domain.trade.service.refund.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.trade.model.entity.MarketPayOrderEntity;
import org.example.domain.trade.model.entity.TradeRefundBehaviorEntity;
import org.example.domain.trade.model.entity.TradeRefundCommandEntity;
import org.example.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import org.example.domain.trade.service.refund.factory.TradeRefundRuleFilterFactory;
import org.springframework.stereotype.Service;

/**
 * @ClassName : UniqueRefundNodeFilter
 * @Description : 重复退单检查
 * @Author : Bingo
 * @Date: 2026/2/26  14:17
 */
@Service
@Slf4j
public class UniqueRefundNodeFilter implements ILogicHandler<TradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> {
    @Override
    public TradeRefundBehaviorEntity apply(TradeRefundCommandEntity tradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("逆向流程-退单操作，重复退单检查 userId:{} outTradeNo:{}", tradeRefundCommandEntity.getUserId(), tradeRefundCommandEntity.getOutTradeNo());

        MarketPayOrderEntity marketPayOrderEntity = dynamicContext.getMarketPayOrderEntity();
        TradeOrderStatusEnumVO tradeOrderStatusEnumVO = marketPayOrderEntity.getTradeOrderStatusEnumVO();

        if (TradeOrderStatusEnumVO.CLOSE.equals(tradeOrderStatusEnumVO)) {
            log.info("逆向流程，退单操作(幂等-重复退单) userId:{} outTradeNo:{}", tradeRefundCommandEntity.getUserId(), tradeRefundCommandEntity.getOutTradeNo());
            return TradeRefundBehaviorEntity.builder()
                    .userId(tradeRefundCommandEntity.getUserId())
                    .orderId(marketPayOrderEntity.getOrderId())
                    .teamId(marketPayOrderEntity.getTeamId())
                    .tradeRefundBehaviorEnum(TradeRefundBehaviorEntity.TradeRefundBehaviorEnum.REPEAT)
                    .build();
        }

        return next(tradeRefundCommandEntity, dynamicContext);
    }
}
