package org.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @ClassName : LockMarketPayOrderResponseDTO
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/6  12:23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockMarketPayOrderResponseDTO {

    /**
     * 拼团ID
     */
    private String teamId;
    /**
     * 预购订单ID
     */
    private String orderId;
    /**
     * 原始价格
     */
    private BigDecimal originalPrice;
    /**
     * 折扣金额
     */
    private BigDecimal deductionPrice;
    /**
     * 支付金额
     */
    private BigDecimal payPrice;
    /**
     * 交易订单状态
     */
    private Integer tradeOrderStatus;

}
