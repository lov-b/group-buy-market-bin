package org.example.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : TradeRuleCommandEntity
 * @Description : 拼团交易命令实体
 * @Author : Bingo
 * @Date: 2026/1/7  10:52
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeLockRuleCommandEntity {

    /** 用户ID */
    private String userId;
    /** 活动ID */
    private Long activityId;
    /** 组队ID */
    private String teamId;
}
