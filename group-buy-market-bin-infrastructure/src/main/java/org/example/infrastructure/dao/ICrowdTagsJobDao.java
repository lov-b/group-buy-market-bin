package org.example.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.infrastructure.dao.po.CrowdTagsJob;

/**
 * @ClassName : ICrowdTagsJobDao
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/28  15:49
 */
@Mapper
public interface ICrowdTagsJobDao {

    CrowdTagsJob queryCrowdTagsJob(CrowdTagsJob crowdTagsJobReq);

}
