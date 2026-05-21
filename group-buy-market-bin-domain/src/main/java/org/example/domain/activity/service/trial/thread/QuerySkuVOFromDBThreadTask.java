package org.example.domain.activity.service.trial.thread;

import org.example.domain.activity.adapter.repository.IActivityRepository;
import org.example.domain.activity.model.valobj.SkuVO;

import java.util.concurrent.Callable;

/**
 * @ClassName : QuerySkuVOFromDBThreadTask
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/25  15:46
 */
public class QuerySkuVOFromDBThreadTask implements Callable<SkuVO> {

    private final String goodsId;
    private final IActivityRepository activityRepository;

    public QuerySkuVOFromDBThreadTask(String goodsId, IActivityRepository activityRepository) {
        this.goodsId = goodsId;
        this.activityRepository = activityRepository;
    }

    @Override
    public SkuVO call() throws Exception {
        return activityRepository.querySkuByGoodsId(goodsId);
    }
}
