package org.example.domain.activity.service.trial.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.domain.activity.model.entity.MarketProductEntity;
import org.example.domain.activity.model.entity.TrialBalanceEntity;
import org.example.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import org.example.domain.activity.model.valobj.TagScopeEnumVO;
import org.example.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import org.example.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassName : tagNode
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/30  13:14
 */
@Slf4j
@Service
public class TagNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {


    @Resource
    private EndNode endNode;

    @Override
    protected TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {

        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = dynamicContext.getGroupBuyActivityDiscountVO();
        String tagId = groupBuyActivityDiscountVO.getTagId();
        boolean visible = groupBuyActivityDiscountVO.isVisible();
        boolean enable = groupBuyActivityDiscountVO.isEnable();

        //未配置人群标签，直接放行展示和参与
        if (StringUtils.isBlank(tagId)) {
            dynamicContext.setVisible(TagScopeEnumVO.VISIBLE.getAllow());
            dynamicContext.setEnable(TagScopeEnumVO.ENABLE.getAllow());
            return router(requestParameter, dynamicContext);
        }

        //检查用户是否符合人群标签，符合即可放行拼团的展示和参与
        boolean isWithin = repository.isTagCrowdRange(tagId, requestParameter.getUserId());

        //前面是没有该字段 没有人群限制 可以直接放行
        //后者是当前用户符合人群范围 可以放行
        dynamicContext.setVisible(visible || isWithin);
        dynamicContext.setEnable(enable || isWithin);
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) {
        return endNode;
    }
}
