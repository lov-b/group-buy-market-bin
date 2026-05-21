package org.example.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.infrastructure.dao.po.CrowdTagsDetail;

/**
 * @ClassName : ICrowdTagsDetailDao
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/28  15:48
 */
@Mapper
public interface ICrowdTagsDetailDao {

    void addCrowdTagsUserId(CrowdTagsDetail crowdTagsDetailReq);

}
