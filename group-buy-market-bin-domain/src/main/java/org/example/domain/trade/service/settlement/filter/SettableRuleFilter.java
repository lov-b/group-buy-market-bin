package org.example.domain.trade.service.settlement.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.trade.adapter.repository.ITradeRepository;
import org.example.domain.trade.model.entity.GroupBuyTeamEntity;
import org.example.domain.trade.model.entity.MarketPayOrderEntity;
import org.example.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import org.example.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import org.example.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import org.example.types.enums.ResponseCode;
import org.example.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @ClassName : SettableRuleFilter
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/9  10:27
 */
@Slf4j
@Service
public class SettableRuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {

        log.info("结算规则过滤-有效时间校验{} outTradeNo:{}", requestParameter.getUserId(), requestParameter.getOutTradeNo());

        //根据拼团用户明细中的部分拼团信息，查询拼团表中的具体拼团信息
        MarketPayOrderEntity marketPayOrderEntity = dynamicContext.getMarketPayOrderEntity();

        GroupBuyTeamEntity groupBuyTeamEntity = repository.queryGroupBuyTeamByTeamId(marketPayOrderEntity.getTeamId());

        //获取外部交易时间 验证是否在拼团活动有效期内
        Date outTradeTime = requestParameter.getOutTradeTime();
        if (!outTradeTime.before(groupBuyTeamEntity.getValidEndTime())) {
            throw new AppException(ResponseCode.E0106);
        }
        dynamicContext.setGroupBuyTeamEntity(groupBuyTeamEntity);

        return next(requestParameter, dynamicContext);
    }
}
