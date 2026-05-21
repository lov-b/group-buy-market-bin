package org.example.domain.trade.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @ClassName : NotifyConfigVO
 * @Description : 回调配置值对象
 * @Author : Bingo
 * @Date: 2026/1/29  23:25
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotifyConfigVO {
    /**
     * 回调方式；MQ、HTTP
     */
    private NotifyTypeEnumVO notifyType;
    /**
     * 回调消息
     */
    private String notifyMQ;
    /**
     * 回调地址
     */
    private String notifyUrl;
}
