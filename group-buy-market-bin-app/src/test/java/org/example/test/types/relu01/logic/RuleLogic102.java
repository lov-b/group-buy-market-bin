package org.example.test.types.relu01.logic;

import cn.bugstack.wrench.design.framework.link.model1.AbstractLogicLink;
import lombok.extern.slf4j.Slf4j;
import org.example.test.types.relu02.factory.Rule02TradeRuleFactory;
import org.springframework.stereotype.Service;

/**
 * @ClassName : RuleLogic102
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/6  19:11
 */
@Slf4j
@Service
public class RuleLogic102 extends AbstractLogicLink<String, Rule02TradeRuleFactory.DynamicContext, String> {

    @Override
    public String apply(String requestParameter, Rule02TradeRuleFactory.DynamicContext dynamicContext) throws Exception {

        log.info("link model01 RuleLogic102");

        return "link model01 单实例链";
    }

}
