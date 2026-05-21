package org.example.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : TradeRefundOrderEntity
 * @Description : 交易退单实体对象
 * @Author : Bingo
 * @Date: 2026/2/12  8:37
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeRefundOrderEntity {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 拼单组队ID
     */
    private String teamId;

    /**
     * 活动ID，MQ回调消息需要这个字段
     */
    private Long activityId;

    /**
     * 预购订单ID
     */
    private String orderId;

    /**
     * 外部订单ID
     */
    private String outTradeNo;

}
