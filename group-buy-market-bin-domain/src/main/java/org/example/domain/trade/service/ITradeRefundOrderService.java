package org.example.domain.trade.service;

import org.example.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import org.example.domain.trade.model.entity.TradeRefundBehaviorEntity;
import org.example.domain.trade.model.entity.TradeRefundCommandEntity;
import org.example.domain.trade.model.valobj.TeamRefundSuccess;

import java.util.List;

/**
 * @ClassName : ITradeRefundOrderService
 * @Description : 退单，逆向流程接口
 * @Author : Bingo
 * @Date: 2026/2/12  9:44
 */
public interface ITradeRefundOrderService {

    TradeRefundBehaviorEntity refundOrder(TradeRefundCommandEntity tradeRefundCommandEntity) throws Exception;

    void restoreTeamLockStock(TeamRefundSuccess teamRefundSuccess) throws Exception;

    List<UserGroupBuyOrderDetailEntity> queryTimeoutUnpaidOrderList();

}
