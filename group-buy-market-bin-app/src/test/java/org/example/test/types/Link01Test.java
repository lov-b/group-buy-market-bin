package org.example.test.types;

import cn.bugstack.wrench.design.framework.link.model1.ILogicLink;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.test.types.relu01.factory.Rule01TradeRuleFactory;
import org.example.test.types.relu02.factory.Rule02TradeRuleFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @ClassName : Link01Test
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/6  19:28
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class Link01Test {

    @Resource
    public Rule01TradeRuleFactory rule01TradeRuleFactory;

    @Test
    public void test_model01_01() throws Exception {
        ILogicLink<String, Rule02TradeRuleFactory.DynamicContext, String> logicLink = rule01TradeRuleFactory.openLogicLink();
        String logic = logicLink.apply("123", new Rule02TradeRuleFactory.DynamicContext());
        log.info("测试结果:{}", JSON.toJSONString(logic));
    }

}
