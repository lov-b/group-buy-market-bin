package org.example.domain.activity.service.trial.thread;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.activity.adapter.repository.IActivityRepository;
import org.example.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import org.example.domain.activity.model.valobj.SCSkuActivityVO;

import javax.annotation.Resource;
import java.util.concurrent.Callable;

/**
 * @ClassName : QueryGroupBuyActivityDiscountVOThreadTask
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/25  15:14
 */
@Slf4j
public class QueryGroupBuyActivityDiscountVOThreadTask implements Callable<GroupBuyActivityDiscountVO> {

    private final String source;
    private final String channel;
    private final String goodsId;
    private final IActivityRepository activityRepository;

    public QueryGroupBuyActivityDiscountVOThreadTask(String source, String channel, String goodsId, IActivityRepository activityRepository) {
        this.source = source;
        this.channel = channel;
        this.goodsId = goodsId;
        this.activityRepository = activityRepository;
    }

    @Override
    public GroupBuyActivityDiscountVO call() throws Exception {

        SCSkuActivityVO scSkuActivityVO = activityRepository.querySCSkuActivityBySCGoodsId(source, channel, goodsId);
        if (null == scSkuActivityVO) return null;
        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = activityRepository.queryGroupBuyActivityDiscountVO(scSkuActivityVO.getActivityId());
        return groupBuyActivityDiscountVO;
    }
}
