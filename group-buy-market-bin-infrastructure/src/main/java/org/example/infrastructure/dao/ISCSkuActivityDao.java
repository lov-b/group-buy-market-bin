package org.example.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.infrastructure.dao.po.SCSkuActivity;

/**
 * @ClassName : ISCSkuActivityDao
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/29  17:03
 */
@Mapper
public interface ISCSkuActivityDao {

    SCSkuActivity querySCSkuActivityBySCGoodsId(SCSkuActivity scSkuActivity);

}
