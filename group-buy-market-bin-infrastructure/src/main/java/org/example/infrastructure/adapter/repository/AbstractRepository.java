package org.example.infrastructure.adapter.repository;

import org.example.infrastructure.dcc.DCCService;
import org.example.infrastructure.redis.IRedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.function.Supplier;

/**
 * @ClassName : AbstractRepository
 * @Description :
 * @Author : Bingo
 * @Date: 2026/2/4  19:46
 */
public abstract class AbstractRepository {

    private Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

    @Resource
    protected IRedisService redisService;

    @Resource
    protected DCCService dccService;

    // 因为返回的对象都不一样，所以返回对象使用泛型
    protected <T> T getFromRedisOrDB(String cacheKey, Supplier<T> dbFallback) {
        if (dccService.isCacheOpenSwitch()) {
            T cacheResult = redisService.getValue(cacheKey);

            if (null != cacheResult) {
                return cacheResult;
            }

            T dbResult = dbFallback.get();
            if (null == dbResult) {
                return null;
            }
            redisService.setValue(cacheKey, dbResult);
            return dbResult;
        } else {
            return dbFallback.get();
        }
    }

    // 因为返回的对象都不一样，所以返回对象使用泛型
    protected <T> T getFromRedisOrDB(String cacheKey, Supplier<T> dbFallback, long expired) {
        if (dccService.isCacheOpenSwitch()) {
            T cacheResult = redisService.getValue(cacheKey);

            if (null != cacheResult) {
                return cacheResult;
            }

            T dbResult = dbFallback.get();
            if (null == dbResult) {
                return null;
            }
            redisService.setValue(cacheKey, dbResult, expired);
            return dbResult;
        } else {
            return dbFallback.get();
        }
    }

}
