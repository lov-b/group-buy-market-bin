package org.example.domain.trade.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @ClassName : GroupBuyProgressVO
 * @Description : 查询进度
 * @Author : Bingo
 * @Date: 2026/1/6  11:04
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyProgressVO {

    /** 目标数量 */
    private Integer targetCount;
    /** 完成数量 */
    private Integer completeCount;
    /** 锁单数量 */
    private Integer lockCount;

}
