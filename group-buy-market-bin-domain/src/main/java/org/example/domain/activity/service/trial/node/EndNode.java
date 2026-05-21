package org.example.domain.activity.service.trial.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.activity.model.entity.MarketProductEntity;
import org.example.domain.activity.model.entity.TrialBalanceEntity;
import org.example.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import org.example.domain.activity.model.valobj.SkuVO;
import org.example.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import org.example.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @ClassName : EndNode
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/23  15:13
 */
@Slf4j
@Service
public class EndNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {


    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {

        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = dynamicContext.getGroupBuyActivityDiscountVO();
        SkuVO skuVO = dynamicContext.getSkuVO();

        BigDecimal deductionPrice = dynamicContext.getDeductionPrice();
        BigDecimal payPrice = dynamicContext.getPayPrice();

        return TrialBalanceEntity.builder()
                .goodsId(skuVO.getGoodsId())
                .goodsName(skuVO.getGoodsName())
                .originalPrice(skuVO.getOriginalPrice())
                .deductionPrice(deductionPrice)
                .payPrice(payPrice)
                .targetCount(groupBuyActivityDiscountVO.getTarget())
                .startTime(groupBuyActivityDiscountVO.getStartTime())
                .endTime(groupBuyActivityDiscountVO.getEndTime())
                .isVisible(dynamicContext.getVisible())
                .isEnable(dynamicContext.getEnable())
                .groupBuyActivityDiscountVO(groupBuyActivityDiscountVO)

                .build();
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) {
        return defaultStrategyHandler;
    }
}
