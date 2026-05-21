package org.example.domain.trade.adapter.repository;

import org.example.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import org.example.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import org.example.domain.trade.model.aggregate.GroupBuyRefundAggregate;
import org.example.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import org.example.domain.trade.model.entity.GroupBuyActivityEntity;
import org.example.domain.trade.model.entity.GroupBuyTeamEntity;
import org.example.domain.trade.model.entity.MarketPayOrderEntity;
import org.example.domain.trade.model.entity.NotifyTaskEntity;
import org.example.domain.trade.model.valobj.GroupBuyProgressVO;

import java.util.List;

/**
 * @ClassName : ITradeRepository
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/6  11:09
 */
public interface ITradeRepository {
    MarketPayOrderEntity queryMarketPayOrderByOutTradeNo(String userId, String outTradeNo);

    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate);

    GroupBuyActivityEntity queryGroupBuyActivityByActivityId(Long activityId);

    Integer queryOrderCountByActivityId(Long activityId, String userId);

    GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId);

    NotifyTaskEntity settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate);

    boolean isSCBlackIntercept(String source, String channel);

    int updateNotifyTaskStatusSuccess(NotifyTaskEntity notifyTaskEntity);

    int updateNotifyTaskStatusError(NotifyTaskEntity notifyTaskEntity);

    int updateNotifyTaskStatusRetry(NotifyTaskEntity notifyTaskEntity);

    List<NotifyTaskEntity> queryUnExecutedNotifyTaskList();
    
    List<NotifyTaskEntity> queryUnExecutedNotifyTaskList(String teamId);

    boolean occupyTeamStock(String teamStockKey, String recoveryTeamStockKey, Integer target, Integer validTime);

    void recoveryTeamStock(String recoveryTeamStockKey, Integer validTime);

    NotifyTaskEntity unpaid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate);

    NotifyTaskEntity paid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate);

    NotifyTaskEntity paidTeam2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate);

    void refund2AddRecovery(String recoveryTeamStockKey, String orderId) throws Exception;

    List<UserGroupBuyOrderDetailEntity> queryTimeoutUnpaidOrderList();

}
