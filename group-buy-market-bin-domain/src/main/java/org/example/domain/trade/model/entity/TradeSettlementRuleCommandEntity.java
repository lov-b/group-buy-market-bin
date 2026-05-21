package org.example.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @ClassName : TradeSettlementRuleCommandEntity
 * @Description : 相当于Settlement责任链的入参
 * @Author : Bingo
 * @Date: 2026/1/8  22:04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSettlementRuleCommandEntity {

    /** 渠道 */
    private String source;
    /** 来源 */
    private String channel;
    /** 用户ID */
    private String userId;
    /** 外部交易单号 */
    private String outTradeNo;
    /** 外部交易时间 */
    private Date outTradeTime;

}
