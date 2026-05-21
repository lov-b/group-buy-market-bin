package org.example.domain.activity.adapter.repository;

import org.example.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import org.example.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import org.example.domain.activity.model.valobj.SCSkuActivityVO;
import org.example.domain.activity.model.valobj.SkuVO;
import org.example.domain.activity.model.valobj.TeamStatisticVO;

import java.util.List;

/**
 * @ClassName : IActivityRepository
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/24  17:01
 */
public interface IActivityRepository {

    GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(Long activityId);

    SkuVO querySkuByGoodsId(String goodsId);

    SCSkuActivityVO querySCSkuActivityBySCGoodsId(String source, String channel, String goodsId);

    boolean isTagCrowdRange(String tagId, String userId);

    boolean downgradeSwitch();

    boolean cutRange(String userId);

    List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailListByOwner(Long activityId, String userId, Integer ownerCount);

    List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailListByRandom(Long activityId, String userId, Integer randomCount);

    TeamStatisticVO queryTeamStatisticByActivityId(Long activityId);
}
