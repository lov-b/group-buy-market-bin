package org.example.domain.trade.service.settlement.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.trade.adapter.repository.ITradeRepository;
import org.example.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import org.example.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import org.example.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import org.example.types.enums.ResponseCode;
import org.example.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassName : SCRuleFilter
 * @Description : 渠道过滤 检查渠道是否在黑名单中 相当于渠道降级
 * @Author : Bingo
 * @Date: 2026/1/9  10:11
 */
@Slf4j
@Service
public class SCRuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;


    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {

        log.info("结算过滤规则-渠道黑名单校验 userId:{} outTradeNo:{}", requestParameter.getUserId(), requestParameter.getOutTradeNo());

        boolean isIntercept = repository.isSCBlackIntercept(requestParameter.getSource(), requestParameter.getChannel());

        //当前渠道不可用 抛出异常
        if (isIntercept) {
            throw new AppException(ResponseCode.E0105);
        }

        return next(requestParameter, dynamicContext);
    }
}
