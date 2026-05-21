package org.example.domain.activity.service;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.activity.adapter.repository.IActivityRepository;
import org.example.domain.activity.model.entity.MarketProductEntity;
import org.example.domain.activity.model.entity.TrialBalanceEntity;
import org.example.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import org.example.domain.activity.model.valobj.TeamStatisticVO;
import org.example.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @ClassName : IndexGroupBuyMarketServiceImpl
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/23  15:23
 */
@Slf4j
@Service
public class IndexGroupBuyMarketServiceImpl implements IIndexGroupBuyMarketService {

    @Resource
    private IActivityRepository repository;

    @Resource
    private DefaultActivityStrategyFactory defaultActivityStrategyFactory;

    @Override
    public TrialBalanceEntity indexMarketTrial(MarketProductEntity marketProduct) throws Exception {

        StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> strategyHandler = defaultActivityStrategyFactory.strategyHandler();

        TrialBalanceEntity trialBalanceEntity = strategyHandler.apply(marketProduct, new DefaultActivityStrategyFactory.DynamicContext());

        return trialBalanceEntity;
    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailList(Long activityId, String userId, Integer ownerCount, Integer randomCount) {

        List<UserGroupBuyOrderDetailEntity> unionAllList = new ArrayList<>();

        // 查询个人拼团数据
        if (0 != ownerCount) {
            List<UserGroupBuyOrderDetailEntity> ownerList = repository.queryInProgressUserGroupBuyOrderDetailListByOwner(activityId, userId, ownerCount);
            log.info("IndexGroupBuyMarketServiceImpl - queryInProgressUserGroupBuyOrderDetailList after queryInProgressUserGroupBuyOrderDetailListByOwner");
            log.info("List<UserGroupBuyOrderDetailEntity> ownerList:{}", ownerList);
            //非空 当前用户团添加到返回列表中
            if (null != ownerList && !ownerList.isEmpty()) {
                unionAllList.addAll(ownerList);
            }
        }

        // 查询其他非个人拼团
        if (0 != randomCount) {
            List<UserGroupBuyOrderDetailEntity> randomList = repository.queryInProgressUserGroupBuyOrderDetailListByRandom(activityId, userId, randomCount);
            log.info("IndexGroupBuyMarketServiceImpl - queryInProgressUserGroupBuyOrderDetailList after queryInProgressUserGroupBuyOrderDetailListByRandom");
            log.info("List<UserGroupBuyOrderDetailEntity> randomList:{}", randomList);
            //非空 随机用户团添加到返回列表中
            if (null != randomList && !randomList.isEmpty()) {
                unionAllList.addAll(randomList);
            }
        }

        return unionAllList;
    }

    @Override
    public TeamStatisticVO queryTeamStatisticByActivityId(Long activityId) {
        return repository.queryTeamStatisticByActivityId(activityId);
    }
}
