package org.example.domain.activity.service.discount;

import org.example.domain.activity.model.valobj.GroupBuyActivityDiscountVO;

import java.math.BigDecimal;

/**
 * @ClassName : IDiscountCalculateService
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/26  17:25
 */
public interface IDiscountCalculateService {

    BigDecimal calculate(String userId, BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount);
}
