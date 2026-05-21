package org.example.domain.activity.service.trial;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import org.example.domain.activity.adapter.repository.IActivityRepository;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @ClassName : AbstractGroupBuyMarketSupport
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/23  15:08
 */
public abstract class AbstractGroupBuyMarketSupport<MarketProductEntity, DynamicContext, TrialBalanceEntity> extends AbstractMultiThreadStrategyRouter<MarketProductEntity, DynamicContext, TrialBalanceEntity> {

    protected long timeout = 5000;

    @Resource
    protected IActivityRepository repository;

    @Override
    protected void multiThread(MarketProductEntity requestParameter, DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {

    }
}
