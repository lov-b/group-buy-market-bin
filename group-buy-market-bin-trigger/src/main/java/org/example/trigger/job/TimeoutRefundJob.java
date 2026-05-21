package org.example.trigger.job;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import org.example.domain.trade.model.entity.TradeRefundCommandEntity;
import org.example.domain.trade.service.ITradeRefundOrderService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName : TimeoutRefundJob
 * @Description : 超时未支付订单退单定时任务
 * @Author : Bingo
 * @Date: 2026/2/27  9:11
 */
@Slf4j
@Service
public class TimeoutRefundJob {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ITradeRefundOrderService tradeRefundOrderService;

    @Scheduled(cron = "0 */1 * * * ?")
    public void exec() {
        // 分布式锁，防止多实例重复执行
        RLock lock = redissonClient.getLock("group_buy_market_timeout_refund_job_exec");

        try {
            boolean isLocked = lock.tryLock(3, 60, TimeUnit.SECONDS);
            if (!isLocked) {
                log.info("超时退单定时任务，获取锁失败，跳过本次执行");
                return;
            }

            log.info("超时退单定时任务开始执行");

            List<UserGroupBuyOrderDetailEntity> timeoutOrderList = tradeRefundOrderService.queryTimeoutUnpaidOrderList();
            if (timeoutOrderList == null || timeoutOrderList.isEmpty()) {
                log.info("超时退单定时任务，未发现超时未支付订单");
                return;
            }

            log.info("超时退单定时任务，发现超时未支付订单数量：{}", timeoutOrderList.size());

            int successCount = 0;
            int failCount = 0;

            for (UserGroupBuyOrderDetailEntity timeoutOrder : timeoutOrderList) {
                try {
                    tradeRefundOrderService.refundOrder(TradeRefundCommandEntity.builder()
                            .userId(timeoutOrder.getUserId())
                            .outTradeNo(timeoutOrder.getOutTradeNo())
                            .source(timeoutOrder.getSource())
                            .channel(timeoutOrder.getChannel())
                            .build());

                    successCount++;

                    log.info("超时订单退单成功，用户ID：{}，交易单号：{}", timeoutOrder.getUserId(), timeoutOrder.getOutTradeNo());
                } catch (Exception e) {
                    failCount++;
                    log.error("超时订单退单失败，用户ID：{}，交易单号：{}，错误信息：{}",
                            timeoutOrder.getUserId(), timeoutOrder.getOutTradeNo(), e.getMessage(), e);
                }
            }

            log.info("超时退单定时任务执行完成，成功：{}，失败：{}", successCount, failCount);
        } catch (Exception e) {
            log.error("超时退单定时任务执行异常", e);
        } finally {
            // 锁被当前线程占用，释放锁
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
