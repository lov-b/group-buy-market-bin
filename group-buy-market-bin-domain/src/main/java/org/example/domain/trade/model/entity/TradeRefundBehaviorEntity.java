package org.example.domain.trade.model.entity;

import lombok.*;

/**
 * @ClassName : TradeRefundBehaviorEntity
 * @Description : 退单行动
 * @Author : Bingo
 * @Date: 2026/2/12  9:45
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeRefundBehaviorEntity {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 组队ID
     */
    private String teamId;

    /**
     * 行为枚举
     */
    private TradeRefundBehaviorEnum tradeRefundBehaviorEnum;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public enum TradeRefundBehaviorEnum {

        SUCCESS("success", "成功"),
        REPEAT("repeat", "重复"),
        FAIL("fail", "失败"),

        ;

        private String code;
        private String info;
    }
}
