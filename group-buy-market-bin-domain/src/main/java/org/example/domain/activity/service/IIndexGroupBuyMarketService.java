package org.example.domain.activity.service;

import org.example.domain.activity.model.entity.MarketProductEntity;
import org.example.domain.activity.model.entity.TrialBalanceEntity;
import org.example.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import org.example.domain.activity.model.valobj.TeamStatisticVO;

import java.util.List;

/**
 * @ClassName : IIndexGroupBuyMarketService
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/23  15:22
 */
public interface IIndexGroupBuyMarketService {

    TrialBalanceEntity indexMarketTrial(MarketProductEntity marketProduct) throws Exception;

    List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailList(Long activityId, String userId, Integer ownerCount, Integer randomCount);

    TeamStatisticVO queryTeamStatisticByActivityId(Long activityId);
}
