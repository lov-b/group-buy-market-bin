package org.example.test.types.relu02.logic;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.test.types.relu02.factory.Rule02TradeRuleFactory;
import org.springframework.stereotype.Service;

/**
 * @ClassName : RuleLogic202
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/6  19:15
 */
@Slf4j
@Service
public class RuleLogic202 implements ILogicHandler<String, Rule02TradeRuleFactory.DynamicContext, XxxResponse> {

    public XxxResponse apply(String requestParameter, Rule02TradeRuleFactory.DynamicContext dynamicContext) throws Exception {

        log.info("link model02 RuleLogic202");

        return new XxxResponse("hi 小傅哥！");
    }
}
