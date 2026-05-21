package org.example.domain.trade.service.lock.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.trade.adapter.repository.ITradeRepository;
import org.example.domain.trade.model.entity.GroupBuyActivityEntity;
import org.example.domain.trade.model.entity.TradeLockRuleCommandEntity;
import org.example.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import org.example.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import org.example.types.enums.ActivityStatusEnumVO;
import org.example.types.enums.ResponseCode;
import org.example.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @ClassName : ActivityUsabilityRuleFilter
 * @Description : 活动的可用性，规则过滤【状态、有效期】
 * @Author : Bingo
 * @Date: 2026/1/7  10:56
 */
@Slf4j
@Service
public class ActivityUsabilityRuleFilter implements ILogicHandler<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;


    @Override
    public TradeLockRuleFilterBackEntity apply(TradeLockRuleCommandEntity requestParameter, TradeLockRuleFilterFactory.DynamicContext dynamicContext) throws Exception {

        log.info("交易规则过滤-活动的可用性校验{} activityId:{}", requestParameter.getUserId(), requestParameter.getActivityId());

        GroupBuyActivityEntity groupBuyActivity = repository.queryGroupBuyActivityByActivityId(requestParameter.getActivityId());

        //验证活动有效 无效抛出异常
        if (!ActivityStatusEnumVO.EFFECTIVE.equals(groupBuyActivity.getStatus())) {
            throw new AppException(ResponseCode.E0101);
        }

        //验证活动有效期
        Date currentTime = new Date();
        if (currentTime.before(groupBuyActivity.getStartTime()) || currentTime.after(groupBuyActivity.getEndTime())) {
            throw new AppException(ResponseCode.E0102);
        }

        dynamicContext.setGroupBuyActivity(groupBuyActivity);

        return next(requestParameter, dynamicContext);
    }
}
