package org.example.domain.trade.service.refund.business.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.trade.adapter.repository.ITradeRepository;
import org.example.domain.trade.model.aggregate.GroupBuyRefundAggregate;
import org.example.domain.trade.model.entity.GroupBuyTeamEntity;
import org.example.domain.trade.model.entity.NotifyTaskEntity;
import org.example.domain.trade.model.entity.TradeRefundOrderEntity;
import org.example.domain.trade.model.valobj.TeamRefundSuccess;
import org.example.domain.trade.service.ITradeTaskService;
import org.example.domain.trade.service.refund.business.AbstractRefundOrderStrategy;
import org.example.domain.trade.service.refund.business.IRefundOrderStrategy;
import org.example.types.enums.GroupBuyOrderEnumVO;
import org.example.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName : PaidTeam2RefundStrategy
 * @Description : 发起退单（已成团&已支付），锁单量-1、完成量-1、组队订单状态更新、发送退单消息（MQ）
 * @Author : Bingo
 * @Date: 2026/2/12  9:03
 */
@Slf4j
@Service("paidTeam2RefundStrategy")
public class PaidTeam2RefundStrategy extends AbstractRefundOrderStrategy {

    @Override
    public void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity) {
        log.info("退单；已支付，已成团 userId:{} teamId:{} orderId:{}", tradeRefundOrderEntity.getUserId(), tradeRefundOrderEntity.getTeamId(), tradeRefundOrderEntity.getOrderId());

        GroupBuyTeamEntity groupBuyTeamEntity = repository.queryGroupBuyTeamByTeamId(tradeRefundOrderEntity.getTeamId());
        Integer completeCount = groupBuyTeamEntity.getCompleteCount();

        // 最后一个拼团订单，则拼团失败，否则状态为拼团成功（存在退单）
        GroupBuyOrderEnumVO groupBuyOrderEnumVO = completeCount == 1 ? GroupBuyOrderEnumVO.FAIL : GroupBuyOrderEnumVO.COMPLETE_FAIL;

        // 1. 退单，已支付&已成团
        NotifyTaskEntity notifyTaskEntity = repository.paidTeam2Refund(GroupBuyRefundAggregate.buildPaidTeam2RefundAggregate(tradeRefundOrderEntity, -1, -1, groupBuyOrderEnumVO));

        // 2. 发送MQ消息
        sendRefundNotifyMessage(notifyTaskEntity, "已支付，已成团");

    }

    @Override
    public void reverseStock(TeamRefundSuccess teamRefundSuccess) {
        log.info("退单；已支付、已成团，队伍组队结束，不需要恢复锁单量 {} {} {}", teamRefundSuccess.getUserId(), teamRefundSuccess.getActivityId(), teamRefundSuccess.getTeamId());
    }
}
