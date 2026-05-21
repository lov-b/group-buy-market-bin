package org.example.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.infrastructure.dao.po.GroupBuyDiscount;

import java.util.List;

/**
 * @ClassName : IGroupBuyDiscountDao
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/23  11:15
 */
@Mapper
public interface IGroupBuyDiscountDao {

    List<GroupBuyDiscount> queryGroupBuyDiscountList();

    GroupBuyDiscount queryGroupBuyActivityDiscountByDiscountId(String discountId);

}
