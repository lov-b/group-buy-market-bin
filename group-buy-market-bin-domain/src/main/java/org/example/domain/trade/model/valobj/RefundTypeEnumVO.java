package org.example.domain.trade.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.types.enums.GroupBuyOrderEnumVO;
import org.example.types.exception.AppException;

import java.util.Arrays;

/**
 * @ClassName : RefundTypeEnumVO
 * @Description : 退单类型枚举
 * @Author : Bingo
 * @Date: 2026/2/12  10:30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum RefundTypeEnumVO {

    UNPAID_UNLOCK("unpaid_unlock", "unpaid2RefundStrategy", "未支付，未成团") {
        @Override
        public boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO, TradeOrderStatusEnumVO tradeOrderStatusEnumVO) {
            return GroupBuyOrderEnumVO.PROGRESS.equals(groupBuyOrderEnumVO) && TradeOrderStatusEnumVO.CREATE.equals(tradeOrderStatusEnumVO);
        }
    },

    PAID_UNFORMED("paid_unformed", "paid2RefundStrategy", "已支付，未成团") {
        @Override
        public boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO, TradeOrderStatusEnumVO tradeOrderStatusEnumVO) {
            return GroupBuyOrderEnumVO.PROGRESS.equals(groupBuyOrderEnumVO) && TradeOrderStatusEnumVO.COMPLETE.equals(tradeOrderStatusEnumVO);
        }
    },

    PAID_FORMED("paid_formed", "paidTeam2RefundStrategy", "已支付，已成团") {
        // 完成、完成含退单，都做此处理
        @Override
        public boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO, TradeOrderStatusEnumVO tradeOrderStatusEnumVO) {
            return (GroupBuyOrderEnumVO.COMPLETE.equals(groupBuyOrderEnumVO) || GroupBuyOrderEnumVO.COMPLETE_FAIL.equals(groupBuyOrderEnumVO))
                    && TradeOrderStatusEnumVO.COMPLETE.equals(tradeOrderStatusEnumVO);
        }
    },
    ;


    private String code;
    private String strategy;
    private String info;

    public static RefundTypeEnumVO getRefundTypeEnumVOByCode(String code) {
        switch (code) {
            case "unpaid_unlock":
                return UNPAID_UNLOCK;
            case "paid_unformed":
                return PAID_UNFORMED;
            case "paid_formed":
                return PAID_FORMED;
        }
        throw new RuntimeException("退单类型枚举值不存在: " + code);
    }

    /**
     * 抽象方法，由每个枚举值实现自己的匹配逻辑
     */
    public abstract boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO, TradeOrderStatusEnumVO tradeOrderStatusEnumVO);


    public static RefundTypeEnumVO getRefundStrategy(GroupBuyOrderEnumVO groupBuyOrderEnumVO, TradeOrderStatusEnumVO tradeOrderStatusEnumVO) {
        return Arrays.stream(values())
                .filter(refundType -> refundType.matches(groupBuyOrderEnumVO, tradeOrderStatusEnumVO))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("不支持的退款状态组合: groupBuyOrderStatus=" + groupBuyOrderEnumVO + ", tradeOrderStatus=" + tradeOrderStatusEnumVO));
    }


    public static RefundTypeEnumVO valueOf(Integer code) {
        switch (code) {
            case 1:
                return UNPAID_UNLOCK;
            case 2:
                return PAID_UNFORMED;
            case 3:
                return PAID_FORMED;
        }
        throw new RuntimeException("退单类型枚举值不存在: " + code);
    }
}
