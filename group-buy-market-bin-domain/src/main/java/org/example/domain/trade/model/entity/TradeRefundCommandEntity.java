package org.example.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : TradeRefundCommandEntity
 * @Description : 退单实体对象
 * @Author : Bingo
 * @Date: 2026/2/12  9:46
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeRefundCommandEntity {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 外部交易单号
     */
    private String outTradeNo;

    /** 渠道 */
    private String source;

    /** 来源 */
    private String channel;

}
