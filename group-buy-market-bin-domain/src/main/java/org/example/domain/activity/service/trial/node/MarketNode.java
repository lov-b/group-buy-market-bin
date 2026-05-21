package org.example.domain.activity.service.trial.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.activity.model.entity.MarketProductEntity;
import org.example.domain.activity.model.entity.TrialBalanceEntity;
import org.example.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import org.example.domain.activity.model.valobj.SkuVO;
import org.example.domain.activity.service.discount.IDiscountCalculateService;
import org.example.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import org.example.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import org.example.domain.activity.service.trial.thread.QueryGroupBuyActivityDiscountVOThreadTask;
import org.example.domain.activity.service.trial.thread.QuerySkuVOFromDBThreadTask;
import org.example.types.enums.ResponseCode;
import org.example.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @ClassName : MarketNode
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/23  15:13
 */
@Slf4j
@Service
public class MarketNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private TagNode tagNode;
    @Resource
    private EndNode endNode;
    @Resource
    private ErrorNode errorNode;
    // 一次性把IDiscountCalculateService接口下的所有实现都注入到当前service容器下，key对应实现服务类命名的名字，如 MJ、N、ZJ、ZK。
    @Resource
    private Map<String, IDiscountCalculateService> discountCalculateServiceMap;

    @Override
    protected void multiThread(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        QueryGroupBuyActivityDiscountVOThreadTask queryGroupBuyActivityDiscountVOThreadTask = new QueryGroupBuyActivityDiscountVOThreadTask(requestParameter.getSource(), requestParameter.getChannel(), requestParameter.getGoodsId(), repository);
        FutureTask<GroupBuyActivityDiscountVO> groupBuyActivityDiscountVOFutureTask = new FutureTask<>(queryGroupBuyActivityDiscountVOThreadTask);
        threadPoolExecutor.execute(groupBuyActivityDiscountVOFutureTask);

        QuerySkuVOFromDBThreadTask querySkuVOFromDBThreadTask = new QuerySkuVOFromDBThreadTask(requestParameter.getGoodsId(), repository);
        FutureTask<SkuVO> skuVOFutureTask = new FutureTask<>(querySkuVOFromDBThreadTask);
        threadPoolExecutor.execute(skuVOFutureTask);

        dynamicContext.setGroupBuyActivityDiscountVO(groupBuyActivityDiscountVOFutureTask.get(timeout, TimeUnit.MINUTES));
        dynamicContext.setSkuVO(skuVOFutureTask.get(timeout, TimeUnit.MINUTES));
        log.info("拼团商品查询试算服务-MarketNode userId:{} 异步线程加载数据「GroupBuyActivityDiscountVO、SkuVO」完成", requestParameter.getUserId());
    }

    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("拼团商品查询试算服务-MarketNode userId:{} requestParameter:{}", requestParameter.getUserId(), JSON.toJSONString(requestParameter));

        //取出上下文数据
        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = dynamicContext.getGroupBuyActivityDiscountVO();
        if (null == groupBuyActivityDiscountVO) {
            return router(requestParameter, dynamicContext);
        }
        GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount = groupBuyActivityDiscountVO.getGroupBuyDiscount();

        //获取Sku
        SkuVO skuVO = dynamicContext.getSkuVO();
        if (null == groupBuyDiscount || null == skuVO) {
            return router(requestParameter, dynamicContext);
        }

        //获取服务信息
        IDiscountCalculateService iDiscountCalculateService = discountCalculateServiceMap.get(groupBuyDiscount.getMarketPlan());
        //折扣存储的服务类型不存在，抛异常
        if (null == iDiscountCalculateService)
            throw new AppException(ResponseCode.E0001.getCode(), ResponseCode.E0001.getInfo());

        BigDecimal payPrice = iDiscountCalculateService.calculate(requestParameter.getUserId(), skuVO.getOriginalPrice(), groupBuyActivityDiscountVO.getGroupBuyDiscount());

        //优惠金额和支付金额
        dynamicContext.setDeductionPrice(skuVO.getOriginalPrice().subtract(payPrice));
        dynamicContext.setPayPrice(payPrice);


        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) {

        if (null == dynamicContext.getGroupBuyActivityDiscountVO()
                || null == dynamicContext.getSkuVO()
                || null == dynamicContext.getDeductionPrice()) {
            return errorNode;
        }

        return tagNode;
    }
}
