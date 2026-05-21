package org.example.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.infrastructure.dao.po.GroupBuyActivity;

import java.util.List;

/**
 * @ClassName : IGroupBuyActivityDao
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/23  11:15
 */
@Mapper
public interface IGroupBuyActivityDao {

    List<GroupBuyActivity> queryGroupBuyActivityList();

    GroupBuyActivity queryValidGroupBuyActivity(GroupBuyActivity groupBuyActivityReq);

    GroupBuyActivity queryValidGroupBuyActivityId(Long activityId);

    GroupBuyActivity queryGroupBuyActivityByActivityId(Long activityId);
}
