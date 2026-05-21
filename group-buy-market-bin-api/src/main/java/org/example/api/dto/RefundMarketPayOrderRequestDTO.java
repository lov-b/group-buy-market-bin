package org.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : RefundMarketPayOrderRequestDTO
 * @Description : 营销拼团退单请求对象
 * @Author : Bingo
 * @Date: 2026/2/26  22:48
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundMarketPayOrderRequestDTO {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 外部交易单号
     */
    private String outTradeNo;

    /**
     * 渠道
     */
    private String source;

    /**
     * 来源
     */
    private String channel;

}
