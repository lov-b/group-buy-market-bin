package org.example.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @ClassName : SCSkuActivityVO
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/29  17:30
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SCSkuActivityVO {

    /** 渠道 */
    private String source;
    /** 来源 */
    private String channel;
    /** 活动ID */
    private Long activityId;
    /** 商品ID */
    private String goodsId;
}
