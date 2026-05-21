package org.example.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.trade.model.valobj.TradeOrderStatusEnumVO;

import java.math.BigDecimal;

/**
 * @ClassName : MarketPayOrderEntity
 * @Description : 锁单服务返回实体
 * @Author : Bingo
 * @Date: 2026/1/6  10:59
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketPayOrderEntity {

    private String teamId;
    /** 预购订单ID */
    private String orderId;
    /** 原始价格 */
    private BigDecimal originalPrice;
    /** 折扣金额 */
    private BigDecimal deductionPrice;
    /** 支付金额 */
    private BigDecimal payPrice;
    /** 交易订单状态枚举对象 */
    private TradeOrderStatusEnumVO tradeOrderStatusEnumVO;

}
