package org.example.domain.tag.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @ClassName : CrowdTagJobEntity
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/28  20:07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrowdTagJobEntity {

    /**
     * 标签类型（参与量、消费金额）
     */
    private Integer tagType;
    /**
     * 标签规则（限定类型 N次）
     */
    private String tagRule;
    /**
     * 统计数据，开始时间
     */
    private Date statStartTime;
    /**
     * 统计数据，结束时间
     */
    private Date statEndTime;

}
