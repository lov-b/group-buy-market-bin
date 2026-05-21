package org.example.domain.trade.service.refund.business;

import org.example.domain.trade.model.entity.TradeRefundOrderEntity;
import org.example.domain.trade.model.valobj.TeamRefundSuccess;

/**
 * @ClassName : IRefundOrderStrategy
 * @Description : 退单策略接口 未支付，Unpaid 未成团，UnformedTeam 已成团，AlreadyFormedTeam
 * @Author : Bingo
 * @Date: 2026/2/12  9:03
 */
public interface IRefundOrderStrategy {

    void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity) throws Exception;

    void reverseStock(TeamRefundSuccess teamRefundSuccess) throws Exception;
}
