package org.example.trigger.listener;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.trade.model.valobj.TeamRefundSuccess;
import org.example.domain.trade.service.ITradeRefundOrderService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName : RefundSuccessTopicListener
 * @Description : 结算完成消息监听
 * @Author : Bingo
 * @Date: 2026/2/14  14:20
 */
@Slf4j
@Component
public class RefundSuccessTopicListener {

    @Resource
    private ITradeRefundOrderService tradeRefundOrderService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${spring.rabbitmq.config.producer.topic_team_refund.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.producer.exchange}", type = ExchangeTypes.TOPIC),
                    key = "${spring.rabbitmq.config.producer.topic_team_refund.routing_key}"
            )
    )
    public void listener(String message) {

        log.info("接收消息（退单成功）- 恢复拼团队伍锁单量:{}", message);

        TeamRefundSuccess teamRefundSuccess = JSON.parseObject(message, TeamRefundSuccess.class);

        try {
            tradeRefundOrderService.restoreTeamLockStock(teamRefundSuccess);
        } catch (Exception e) {
            log.info("接收消息（退单成功）- 恢复拼团队伍锁单量失败:{}", message, e);
            // 抛异常，mq消息会重试
            throw new RuntimeException(e);
        }
    }
}
