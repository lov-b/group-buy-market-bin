package org.example.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : MarketProductEntity
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/23  15:02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketProductEntity {

    private Long activityId;
    /** 用户ID */
    private String userId;
    /** 商品ID */
    private String goodsId;
    /** 渠道 */
    private String source;
    /** 来源 */
    private String channel;

}
