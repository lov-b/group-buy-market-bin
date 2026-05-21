package org.example.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : UserEntity
 * @Description : 用户实体对象
 * @Author : Bingo
 * @Date: 2026/1/6  10:53
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    private String userId;

}
