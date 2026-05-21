package org.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : SettlementMarketPayOrderResponseDTO
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/12  17:09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettlementMarketPayOrderResponseDTO {

    /** 用户ID */
    private String userId;
    /** 拼单组队ID */
    private String teamId;
    /** 活动ID */
    private Long activityId;
    /** 外部交易单号 */
    private String outTradeNo;
}
