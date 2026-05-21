package org.example.domain.tag.service;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.tag.adapter.repository.ITagRepository;
import org.example.domain.tag.model.entity.CrowdTagJobEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName : TagService
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/28  20:05
 */
@Slf4j
@Service
public class TagService implements ITagService {

    @Resource
    private ITagRepository repository;

    @Override
    public void execTagBatchJob(String tagId, String batchId) {
        log.info("人群标签批次任务 tagId:{} batchId:{}", tagId, batchId);
        // 1. 查询批次任务
        CrowdTagJobEntity crowdTagJobEntity = repository.queryCrowdTagJobEntity(tagId, batchId);

        // 2. 采集用户数据
        List<String> userIdList = new ArrayList<String>() {{
            add("xiaofuge");
            add("liergou");
            add("bin");
            add("bingo");
            add("xfg01");
        }};

        for (String userId : userIdList) {
            repository.addCrowdTagsUserId(tagId, userId);
        }

        // 3.更新人群标签累积量
        repository.updateCrowdTagsStatistics(tagId, userIdList.size());

    }

}
