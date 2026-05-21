package org.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName : NotifyRequestDTO
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/9  16:51
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyRequestDTO {

    /**
     * 组队ID
     */
    private String teamId;
    /**
     * 外部单号
     */
    private List<String> outTradeNoList;

}
