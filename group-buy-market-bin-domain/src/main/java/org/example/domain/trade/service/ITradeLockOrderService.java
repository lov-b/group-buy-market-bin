package org.example.domain.trade.service;

import org.example.domain.trade.model.entity.MarketPayOrderEntity;
import org.example.domain.trade.model.entity.PayActivityEntity;
import org.example.domain.trade.model.entity.PayDiscountEntity;
import org.example.domain.trade.model.entity.UserEntity;
import org.example.domain.trade.model.valobj.GroupBuyProgressVO;

/**
 * @ClassName : ITradeOrderService
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/6  10:52
 */
public interface ITradeLockOrderService {

    /**
     * 查询，未被支付消费完成的营销优惠订单
     *
     * @param userId
     * @param outTradeNo
     * @return
     */
    MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo);

    /**
     * 查询拼团进度
     *
     * @param teamId
     * @return
     */
    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    /**
     * 锁定，营销预支付订单；商品下单前，预购锁定。
     *
     * @param userEntity
     * @param payActivityEntity
     * @param payDiscountEntity
     * @return
     */
    MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity, PayDiscountEntity payDiscountEntity) throws Exception;

}
