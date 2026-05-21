package org.example.trigger.job;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.trade.service.ITradeSettlementOrderService;
import org.example.domain.trade.service.ITradeTaskService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName : GroupBuyNotifyJob
 * @Description : 定时任务 定时执行未处理回调任务 拼团完结回调通知任务；拼团回调任务表，实际公司场景会定时清理数据结转，不会有太多数据挤压
 * @Author : Bingo
 * @Date: 2026/1/11  13:44
 */
@Slf4j
@Service
public class GroupBuyNotifyJob {

    @Resource
    private ITradeTaskService tradeTaskService;

    @Resource
    private RedissonClient redissonClient;

    @Scheduled(cron = "0/15 * * * * ?")
    public void exec() {
        RLock lock = redissonClient.getLock("group_buy_market_notify_job_exec");
        try {
            boolean isLocked = lock.tryLock(3, 0, TimeUnit.SECONDS);
            if (!isLocked) {
                return;
            }

            Map<String, Integer> result = tradeTaskService.execNotifyJob();
            log.info("定时任务，回调通知拼团完结任务 result:{}", JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("定时任务，回调通知拼团完结任务失败", e);
        } finally {
            //  当前线程确实持有这把锁的情况下，才去解锁。避免异常和误删别人的锁。
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
