package org.example.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.infrastructure.dao.po.GroupBuyOrderList;

import java.util.List;

/**
 * @ClassName : IGroupBuyOrderListDao
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/4  15:54
 */
@Mapper
public interface IGroupBuyOrderListDao {

    void insert(GroupBuyOrderList groupBuyOrderListReq);

    GroupBuyOrderList queryGroupBuyOrderRecordByOutTradeNo(GroupBuyOrderList groupBuyOrderListReq);

    Integer queryOrderCountByActivityId(GroupBuyOrderList groupBuyOrderListReq);

    int updateOrderStatus2COMPLETE(GroupBuyOrderList groupBuyOrderListReq);

    List<String> queryGroupBuyCompleteOrderOutTradeNoListByTeamId(String teamId);

    List<GroupBuyOrderList> queryInProgressUserGroupBuyOrderDetailListByUserId(GroupBuyOrderList groupBuyOrderListReq);

    List<GroupBuyOrderList> queryInProgressUserGroupBuyOrderDetailListByRandom(GroupBuyOrderList groupBuyOrderListReq);

    List<GroupBuyOrderList> queryInProgressUserGroupBuyOrderDetailListByActivityId(Long activityId);

    int unpaid2Refund(GroupBuyOrderList groupBuyOrderListReq);

    int paid2Refund(GroupBuyOrderList groupBuyOrderListReq);

    int paidTeam2Refund(GroupBuyOrderList groupBuyOrderListReq);

    List<GroupBuyOrderList> queryTimeoutUnpaidOrderList();
}
