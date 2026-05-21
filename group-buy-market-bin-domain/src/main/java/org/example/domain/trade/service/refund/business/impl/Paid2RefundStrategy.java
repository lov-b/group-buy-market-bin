package org.example.domain.trade.service.refund.business.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.trade.adapter.port.ITradePort;
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
 * @ClassName : Paid2RefundStrategy
 * @Description : 发起退单（未成团 &已支付），锁单量-1、完成量-1、组队订单状态更新、发送退单消息（MQ）
 * @Author : Bingo
 * @Date: 2026/2/12  9:02
 */
@Slf4j
@Service("paid2RefundStrategy")
public class Paid2RefundStrategy extends AbstractRefundOrderStrategy{

    @Override
    public void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity) throws Exception {

        // 1. 退单，已支付&未成团
        NotifyTaskEntity notifyTaskEntity = repository.paid2Refund(GroupBuyRefundAggregate.buildPaid2RefundAggregate(tradeRefundOrderEntity, -1, -1));

        // 2. 发送MQ消息
        sendRefundNotifyMessage(notifyTaskEntity, "已支付，未成团");
    }

    @Override
    public void reverseStock(TeamRefundSuccess teamRefundSuccess) throws Exception {
        doReverseStock(teamRefundSuccess, "已支付，未成团，但有锁单记录，要恢复锁单库存");
    }
}
