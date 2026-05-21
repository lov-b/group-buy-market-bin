package org.example.domain.trade.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @ClassName : NotifyTypeEnumVO
 * @Description : 回调方式枚举
 * @Author : Bingo
 * @Date: 2026/1/29  23:25
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum NotifyTypeEnumVO {
    HTTP("HTTP", "HTTP 回调"),
    MQ("MQ", "MQ 消息通知"),
    ;

    private String code;
    private String info;
}
