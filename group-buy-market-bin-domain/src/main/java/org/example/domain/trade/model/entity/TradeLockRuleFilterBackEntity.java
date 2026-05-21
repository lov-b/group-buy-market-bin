package org.example.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : TradeRuleFilterBackEntity
 * @Description : 拼团交易，过滤反馈实体
 * @Author : Bingo
 * @Date: 2026/1/7  10:53
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeLockRuleFilterBackEntity {

    // 用户参与活动的订单量
    private Integer userTakeOrderCount;
    // 恢复组队库存缓存key
    private String recoveryTeamStockKey;
}
