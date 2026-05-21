package org.example.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.infrastructure.dao.po.CrowdTags;

/**
 * @ClassName : ICrowdTagsDao
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/28  15:48
 */
@Mapper
public interface ICrowdTagsDao {

    void updateCrowdTagsStatistics(CrowdTags crowdTagsReq);

}
