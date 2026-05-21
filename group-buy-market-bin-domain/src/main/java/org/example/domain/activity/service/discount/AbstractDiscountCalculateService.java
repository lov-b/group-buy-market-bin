package org.example.domain.activity.service.discount;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.activity.adapter.repository.IActivityRepository;
import org.example.domain.activity.model.valobj.GroupBuyActivityDiscountVO;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @ClassName : AbstractDiscountCalculateService
 * @Description : 过滤人群标签（优惠试算步骤之一）
 * @Author : Bingo
 * @Date: 2025/12/26  17:38
 */
@Slf4j
public abstract class AbstractDiscountCalculateService implements IDiscountCalculateService {


    @Resource
    protected IActivityRepository repository;

    @Override
    public BigDecimal calculate(String userId, BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount) {

        boolean isCrowdRange = filterTagId(userId, groupBuyDiscount.getTagId());
        if (!isCrowdRange) {
            log.info("折扣优惠计算拦截 user id:{}", userId);
            return originalPrice;
        }

        return doCalculate(originalPrice, groupBuyDiscount);
    }

    private boolean filterTagId(String userId, String tagId) {
        return repository.isTagCrowdRange(userId, tagId);
    }

    protected abstract BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount);
}
