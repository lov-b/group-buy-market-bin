package org.example.api.dto;

import lombok.Data;

/**
 * @ClassName : GoodsMarketRequestDTO
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/12  20:18
 */
@Data
public class GoodsMarketRequestDTO {

    // 用户ID
    private String userId;
    // 渠道
    private String source;
    // 来源
    private String channel;
    // 商品ID
    private String goodsId;

}
