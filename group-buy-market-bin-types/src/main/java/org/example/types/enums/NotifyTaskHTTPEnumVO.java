package org.example.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @ClassName : NotifyTaskHTTPEnumVO
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/9  16:18
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum NotifyTaskHTTPEnumVO {

    SUCCESS("success", "成功"),
    ERROR("error", "失败"),
    NULL(null, "空执行"),
    ;

    private String code;
    private String info;

}
