package org.example.test.types.relu02.logic;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.test.types.relu02.factory.Rule02TradeRuleFactory;
import org.springframework.stereotype.Service;

/**
 * @ClassName : RuleLogic201
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/6  19:14
 */
@Slf4j
@Service
public class RuleLogic201 implements ILogicHandler<String, Rule02TradeRuleFactory.DynamicContext, XxxResponse> {

    public XxxResponse apply(String requestParameter, Rule02TradeRuleFactory.DynamicContext dynamicContext) throws Exception {

        log.info("link model02 RuleLogic201");

        return next(requestParameter, dynamicContext);
    }

}
