package org.example.domain.tag.adapter.repository;

import org.example.domain.tag.model.entity.CrowdTagJobEntity;

/**
 * @ClassName : ITagRepository
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/28  20:06
 */
public interface ITagRepository {

    CrowdTagJobEntity queryCrowdTagJobEntity(String tagId, String batchId);

    void addCrowdTagsUserId(String tagId, String userId);

    void updateCrowdTagsStatistics(String tagId, int size);
    
}
