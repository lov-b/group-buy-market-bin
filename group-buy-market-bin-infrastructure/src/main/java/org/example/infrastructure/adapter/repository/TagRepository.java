package org.example.infrastructure.adapter.repository;

import org.example.domain.tag.adapter.repository.ITagRepository;
import org.example.domain.tag.model.entity.CrowdTagJobEntity;
import org.example.infrastructure.dao.ICrowdTagsDao;
import org.example.infrastructure.dao.ICrowdTagsDetailDao;
import org.example.infrastructure.dao.ICrowdTagsJobDao;
import org.example.infrastructure.dao.po.CrowdTags;
import org.example.infrastructure.dao.po.CrowdTagsDetail;
import org.example.infrastructure.dao.po.CrowdTagsJob;
import org.example.infrastructure.redis.IRedisService;
import org.redisson.api.RBitSet;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @ClassName : TagRepository
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/28  20:16
 */
@Repository
public class TagRepository implements ITagRepository {

    @Resource
    private ICrowdTagsDao crowdTagsDao;
    @Resource
    private ICrowdTagsDetailDao crowdTagsDetailDao;
    @Resource
    private ICrowdTagsJobDao crowdTagsJobDao;
    @Resource
    private IRedisService redisService;

    @Override
    public CrowdTagJobEntity queryCrowdTagJobEntity(String tagId, String batchId) {

        CrowdTagsJob crowdTagsJobReq = new CrowdTagsJob();
        crowdTagsJobReq.setTagId(tagId);
        crowdTagsJobReq.setBatchId(batchId);

        CrowdTagsJob crowdTagsJobRes = crowdTagsJobDao.queryCrowdTagsJob(crowdTagsJobReq);

        return CrowdTagJobEntity.builder().tagType(crowdTagsJobRes.getTagType()).tagRule(crowdTagsJobRes.getTagRule()).statStartTime(crowdTagsJobRes.getStatStartTime()).statEndTime(crowdTagsJobRes.getStatEndTime()).build();
    }

    @Override
    public void addCrowdTagsUserId(String tagId, String userId) {

        CrowdTagsDetail crowdTagsDetailReq = new CrowdTagsDetail();
        crowdTagsDetailReq.setTagId(tagId);
        crowdTagsDetailReq.setUserId(userId);

        try {
            //存储到数据库
            crowdTagsDetailDao.addCrowdTagsUserId(crowdTagsDetailReq);  // 可能会产生唯一索引冲突
        } catch (DuplicateKeyException ignore) {
        }

        //存储到redis
        RBitSet bitSet = redisService.getBitSet(tagId);
        bitSet.set(redisService.getIndexFromUserId(userId));

    }

    @Override
    public void updateCrowdTagsStatistics(String tagId, int size) {
        CrowdTags crowdTagsReq = new CrowdTags();
        crowdTagsReq.setTagId(tagId);
        crowdTagsReq.setStatistics(size);

        crowdTagsDao.updateCrowdTagsStatistics(crowdTagsReq);
    }
}
