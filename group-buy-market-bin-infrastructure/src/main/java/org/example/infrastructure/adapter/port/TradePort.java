package org.example.infrastructure.adapter.port;

import com.esotericsoftware.minlog.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.domain.trade.adapter.port.ITradePort;
import org.example.domain.trade.model.entity.NotifyTaskEntity;
import org.example.domain.trade.model.valobj.NotifyTypeEnumVO;
import org.example.infrastructure.event.EventPublisher;
import org.example.infrastructure.gateway.GroupBuyNotifyService;
import org.example.infrastructure.redis.IRedisService;
import org.example.types.enums.NotifyTaskHTTPEnumVO;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName : TradePort
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/11  11:46
 */
@Slf4j
@Service
public class TradePort implements ITradePort {

    @Resource
    private GroupBuyNotifyService groupBuyNotifyService;
    @Resource
    private IRedisService redisService;
    @Resource
    private EventPublisher publisher;

    @Override
    public String groupBuyNotify(NotifyTaskEntity notifyTask) throws Exception {
        RLock lock = redisService.getLock(notifyTask.lockKey());

        try {
            // 最多等 3秒 去抢锁,锁的自动释放时间（租约）为 0
            if (lock.tryLock(3, 0, TimeUnit.SECONDS)) {
                try {
                    // 回调方式 HTTP
                    if (NotifyTypeEnumVO.HTTP.getCode().equals(notifyTask.getNotifyType())) {
                        // 无效的 notifyUrl 则直接返回成功
                        if (StringUtils.isBlank(notifyTask.getNotifyUrl()) || "暂无".equals(notifyTask.getNotifyUrl())) {
                            return NotifyTaskHTTPEnumVO.SUCCESS.getCode();
                        }
                        return groupBuyNotifyService.groupBuyNotify(notifyTask.getNotifyUrl(), notifyTask.getParameterJson());
                    }

                    // 回调方式 MQ
                    if (NotifyTypeEnumVO.MQ.getCode().equals(notifyTask.getNotifyType())) {
                        publisher.publish(notifyTask.getNotifyMQ(), notifyTask.getParameterJson());
                        return NotifyTaskHTTPEnumVO.SUCCESS.getCode();
                    }
                } finally {
                    //使用完后释放锁
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
            log.info("TradePort - groupBuyNotify - return NotifyTaskHTTPEnumVO.NULL.getCode();");
            return NotifyTaskHTTPEnumVO.NULL.getCode();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.info("TradePort - groupBuyNotify - throw -  return NotifyTaskHTTPEnumVO.NULL.getCode();");
            return NotifyTaskHTTPEnumVO.NULL.getCode();
        }
    }
}
