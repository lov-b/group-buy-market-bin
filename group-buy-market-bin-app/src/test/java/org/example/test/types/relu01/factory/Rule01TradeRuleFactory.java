package org.example.test.types.relu01.factory;

import cn.bugstack.wrench.design.framework.link.model1.ILogicLink;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.test.types.relu01.logic.RuleLogic101;
import org.example.test.types.relu01.logic.RuleLogic102;
import org.example.test.types.relu02.factory.Rule02TradeRuleFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassName : Rule01TradeRuleFactory
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/6  19:12
 */
@Service
public class Rule01TradeRuleFactory {

    @Resource
    private RuleLogic101 ruleLogic101;
    @Resource
    private RuleLogic102 ruleLogic102;

    /**
     * 两个节点形成一个链
     *
     * @return
     */
    public ILogicLink<String, Rule02TradeRuleFactory.DynamicContext, String> openLogicLink() {
        ruleLogic101.appendNext(ruleLogic102);
        return ruleLogic101;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {
        private String age;
    }

}
