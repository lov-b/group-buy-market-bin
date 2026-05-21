package org.example.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @ClassName : Sku
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/24  17:04
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkuVO {

    /**
     * 商品ID
     */
    private String goodsId;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 原始价格
     */
    private BigDecimal originalPrice;
}
