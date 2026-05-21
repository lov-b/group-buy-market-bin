package org.example.domain.trade.service.lock.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.trade.adapter.repository.ITradeRepository;
import org.example.domain.trade.model.entity.GroupBuyActivityEntity;
import org.example.domain.trade.model.entity.TradeLockRuleCommandEntity;
import org.example.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import org.example.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import org.example.types.enums.ResponseCode;
import org.example.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassName : UserTakeLimitRuleFilter
 * @Description : 用户参与限制，规则过滤
 * @Author : Bingo
 * @Date: 2026/1/7  10:56
 */
@Slf4j
@Service
public class UserTakeLimitRuleFilter implements ILogicHandler<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

    @Override
    public TradeLockRuleFilterBackEntity apply(TradeLockRuleCommandEntity requestParameter, TradeLockRuleFilterFactory.DynamicContext dynamicContext) throws Exception {

        log.info("交易规则过滤-用户参与次数校验{} activityId:{}", requestParameter.getUserId(), requestParameter.getActivityId());

        GroupBuyActivityEntity groupBuyActivity = dynamicContext.getGroupBuyActivity();

        // 查询用户在一个拼团活动上参与的次数
        Integer count = repository.queryOrderCountByActivityId(requestParameter.getActivityId(), requestParameter.getUserId());

        //超过参与次数 抛出对应异常
        if (null != groupBuyActivity.getTakeLimitCount() && count >= groupBuyActivity.getTakeLimitCount()) {
            throw new AppException(ResponseCode.E0103);
        }

        dynamicContext.setUserTakeOrderCount(count);

        return next(requestParameter, dynamicContext);
    }
}
