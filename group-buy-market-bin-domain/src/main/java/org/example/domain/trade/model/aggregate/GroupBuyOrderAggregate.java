package org.example.domain.trade.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.trade.model.entity.PayActivityEntity;
import org.example.domain.trade.model.entity.PayDiscountEntity;
import org.example.domain.trade.model.entity.UserEntity;

/**
 * @ClassName : GroupBuyOrderAggregate
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/6  11:13
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyOrderAggregate {

    /** 用户实体对象 */
    private UserEntity userEntity;
    /** 支付活动实体对象 */
    private PayActivityEntity payActivityEntity;
    /** 支付优惠实体对象 */
    private PayDiscountEntity payDiscountEntity;
    /** 已参与拼团量 */
    private Integer userTakeOrderCount;

}
