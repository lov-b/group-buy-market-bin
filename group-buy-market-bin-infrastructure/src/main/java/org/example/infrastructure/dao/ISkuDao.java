package org.example.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.infrastructure.dao.po.Sku;

/**
 * @ClassName : SkuDao
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/25  10:55
 */
@Mapper
public interface ISkuDao {

    Sku querySkuByGoodsId(String goodsId);

}
