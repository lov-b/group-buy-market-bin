package org.example.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.trade.model.valobj.NotifyConfigVO;

import java.math.BigDecimal;

/**
 * @ClassName : PayDiscountEntity
 * @Description : 支付优惠实体
 * @Author : Bingo
 * @Date: 2026/1/6  10:54
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayDiscountEntity {

    /** 渠道 */
    private String source;
    /** 来源 */
    private String channel;
    /** 商品ID */
    private String goodsId;
    /** 商品名称 */
    private String goodsName;
    /** 原始价格 */
    private BigDecimal originalPrice;
    /** 折扣金额 */
    private BigDecimal deductionPrice;
    /** 支付金额 */
    private BigDecimal payPrice;
    /** 外部交易单号-确保外部调用唯一幂等 */
    private String outTradeNo;
    /** 回调配置 */
    private NotifyConfigVO notifyConfigVO;

}
