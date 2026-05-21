package org.example.domain.trade.service.refund.business.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.trade.adapter.repository.ITradeRepository;
import org.example.domain.trade.model.aggregate.GroupBuyRefundAggregate;
import org.example.domain.trade.model.entity.NotifyTaskEntity;
import org.example.domain.trade.model.entity.TradeRefundOrderEntity;
import org.example.domain.trade.model.valobj.TeamRefundSuccess;
import org.example.domain.trade.service.ITradeTaskService;
import org.example.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import org.example.domain.trade.service.refund.business.AbstractRefundOrderStrategy;
import org.example.domain.trade.service.refund.business.IRefundOrderStrategy;
import org.example.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName : Unpaid2RefundStrategy
 * @Description : 未支付，未成团；发起退单（未支付），锁单量-1、组队订单状态更新
 * @Author : Bingo
 * @Date: 2026/2/12  9:04
 */
@Slf4j
@Service("unpaid2RefundStrategy")
public class Unpaid2RefundStrategy extends AbstractRefundOrderStrategy {

    @Override
    public void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity) {
        log.info("退单；未支付，未成团 userId:{} teamId:{} orderId:{}", tradeRefundOrderEntity.getUserId(), tradeRefundOrderEntity.getTeamId(), tradeRefundOrderEntity.getOrderId());
        // 1. 退单；未支付，未成团
        NotifyTaskEntity notifyTaskEntity = repository.unpaid2Refund(GroupBuyRefundAggregate.buildUnpaid2RefundAggregate(tradeRefundOrderEntity, -1));

        // 2. 发送MQ消息 - 发送MQ，恢复锁单库存量使用
        sendRefundNotifyMessage(notifyTaskEntity, "未支付，未成团");

    }

    @Override
    public void reverseStock(TeamRefundSuccess teamRefundSuccess) throws Exception {
        log.info("退单；恢复锁单量 - 未支付，未成团，但有锁单记录，要恢复锁单库存 {} {} {}", teamRefundSuccess.getUserId(), teamRefundSuccess.getActivityId(), teamRefundSuccess.getTeamId());
        doReverseStock(teamRefundSuccess, "未支付，未成团，但有锁单记录，要恢复锁单库存");
    }
}
