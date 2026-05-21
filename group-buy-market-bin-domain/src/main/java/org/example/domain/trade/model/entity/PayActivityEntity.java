package org.example.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @ClassName : PayActivityEntity
 * @Description : 支付活动实体对象
 * @Author : Bingo
 * @Date: 2026/1/6  10:53
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayActivityEntity {

    /**
     * 拼单组队ID
     */
    private String teamId;
    /**
     * 活动ID
     */
    private Long activityId;
    /**
     * 活动名称
     */
    private String activityName;
    /**
     * 拼团开始时间
     */
    private Date startTime;
    /**
     * 拼团结束时间
     */
    private Date endTime;
    /**
     * 拼团时长（分钟）
     */
    private Integer validTime;
    /**
     * 目标数量
     */
    private Integer targetCount;

}
