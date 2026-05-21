package org.example.domain.trade.service.settlement.factory;

import cn.bugstack.wrench.design.framework.link.model2.LinkArmory;
import cn.bugstack.wrench.design.framework.link.model2.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.trade.model.entity.GroupBuyTeamEntity;
import org.example.domain.trade.model.entity.MarketPayOrderEntity;
import org.example.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import org.example.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import org.example.domain.trade.service.settlement.filter.EndRuleFilter;
import org.example.domain.trade.service.settlement.filter.OutTradeNoRuleFilter;
import org.example.domain.trade.service.settlement.filter.SCRuleFilter;
import org.example.domain.trade.service.settlement.filter.SettableRuleFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * @ClassName : TradeSettlementRuleFilterFactory
 * @Description : Settlement责任链装配工厂 调用LinkAromy
 * @Author : Bingo
 * @Date: 2026/1/8  22:01
 */
@Slf4j
@Service
public class TradeSettlementRuleFilterFactory {

    @Bean("tradeSettlementRuleFilter")
    public BusinessLinkedList<TradeSettlementRuleCommandEntity,
                DynamicContext, TradeSettlementRuleFilterBackEntity> tradeSettlementRuleFilter(
            SCRuleFilter scRuleFilter, OutTradeNoRuleFilter outTradeNoRuleFilter,
            SettableRuleFilter settableRuleFilter, EndRuleFilter endRuleFilter
    ) {
        LinkArmory<TradeSettlementRuleCommandEntity,
                        DynamicContext, TradeSettlementRuleFilterBackEntity> linkArmory
                = new LinkArmory<>("交易结算规则过滤", scRuleFilter, outTradeNoRuleFilter,
                settableRuleFilter, endRuleFilter);

        return linkArmory.getLogicLink();
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicContext {

        private MarketPayOrderEntity marketPayOrderEntity;

        private GroupBuyTeamEntity groupBuyTeamEntity;
    }
}
