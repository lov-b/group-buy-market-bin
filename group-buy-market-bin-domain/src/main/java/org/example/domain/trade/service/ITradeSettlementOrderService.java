package org.example.domain.trade.service;

import org.example.domain.trade.model.entity.NotifyTaskEntity;
import org.example.domain.trade.model.entity.TradePaySettlementEntity;
import org.example.domain.trade.model.entity.TradePaySuccessEntity;

import java.util.Map;

/**
 * @ClassName : ITradeSettlementOrderService
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/7  21:17
 */
public interface ITradeSettlementOrderService {

    /**
     * 支付成功消息作为入参
     */
    TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception;
}
