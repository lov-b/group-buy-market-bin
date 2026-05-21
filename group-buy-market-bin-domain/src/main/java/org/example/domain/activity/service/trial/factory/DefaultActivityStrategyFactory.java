package org.example.domain.activity.service.trial.factory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.activity.model.entity.MarketProductEntity;
import org.example.domain.activity.model.entity.TrialBalanceEntity;
import org.example.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import org.example.domain.activity.model.valobj.SkuVO;
import org.example.domain.activity.service.trial.node.RootNode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @ClassName : DefaultActivityStrategyFactory
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/23  15:11
 */
@Service
public class DefaultActivityStrategyFactory {

    private final RootNode rootNode;

    public DefaultActivityStrategyFactory(RootNode rootNode) {
        this.rootNode = rootNode;
    }

    public StrategyHandler<MarketProductEntity, DynamicContext, TrialBalanceEntity> strategyHandler() {
        return rootNode;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor

    public static class DynamicContext {
        private GroupBuyActivityDiscountVO groupBuyActivityDiscountVO;
        private SkuVO skuVO;
        private BigDecimal deductionPrice;
        private BigDecimal payPrice;
        private Boolean visible;
        private Boolean enable;
    }


}
