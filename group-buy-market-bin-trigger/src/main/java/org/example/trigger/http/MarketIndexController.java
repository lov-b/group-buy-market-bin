package org.example.trigger.http;

import cn.bugstack.wrench.rate.limiter.types.annotations.RateLimiterAccessInterceptor;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.api.IMarketIndexService;
import org.example.api.dto.GoodsMarketRequestDTO;
import org.example.api.dto.GoodsMarketResponseDTO;
import org.example.api.response.Response;
import org.example.domain.activity.model.entity.MarketProductEntity;
import org.example.domain.activity.model.entity.TrialBalanceEntity;
import org.example.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import org.example.domain.activity.model.valobj.TeamStatisticVO;
import org.example.domain.activity.service.IIndexGroupBuyMarketService;
import org.example.types.enums.ResponseCode;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName : MarketIndexController
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/12  21:23
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/index/")
public class MarketIndexController implements IMarketIndexService {

    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;

    @RateLimiterAccessInterceptor(key = "userId", fallbackMethod = "queryGroupBuyMarketConfigFallBack", permitsPerSecond = 1.0d, blacklistCount = 1)
    @RequestMapping(value = "query_group_buy_market_config", method = RequestMethod.POST)
    @Override
    public Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(@RequestBody GoodsMarketRequestDTO requestDTO) {

        try {
            log.info("查询拼团营销配置开始:{} goodsId:{}", requestDTO.getUserId(), requestDTO.getGoodsId());
            //合法性检验
            if (StringUtils.isBlank(requestDTO.getUserId()) || StringUtils.isBlank(requestDTO.getSource()) || StringUtils.isBlank(requestDTO.getChannel()) || StringUtils.isBlank(requestDTO.getGoodsId())) {
                return Response.<GoodsMarketResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            // 1. 营销优惠试算
            MarketProductEntity marketProduct = MarketProductEntity.builder()
                    .userId(requestDTO.getUserId())
                    .goodsId(requestDTO.getGoodsId())
                    .source(requestDTO.getSource())
                    .channel(requestDTO.getChannel())
                    .build();
            TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(marketProduct);
            Long activityId = trialBalanceEntity.getGroupBuyActivityDiscountVO().getActivityId();

            // 2. 查询拼团组队 - 根据activityId
            log.info("查询拼团组队 - 根据activityId :{}", activityId);
            List<UserGroupBuyOrderDetailEntity> userGroupBuyOrderDetailEntities = indexGroupBuyMarketService.queryInProgressUserGroupBuyOrderDetailList(activityId, requestDTO.getUserId(), 1, 2);
            log.info("after 查询拼团组队 userGroupBuyOrderDetailEntities:{}", userGroupBuyOrderDetailEntities);

            // 3. 统计拼团数据
            TeamStatisticVO teamStatisticVO = indexGroupBuyMarketService.queryTeamStatisticByActivityId(activityId);

            // 4.封装回调数据
            // 商品信息
            GoodsMarketResponseDTO.Goods goods = new GoodsMarketResponseDTO.Goods();
            goods.setGoodsId(marketProduct.getGoodsId());
            goods.setDeductionPrice(trialBalanceEntity.getDeductionPrice());
            goods.setOriginalPrice(trialBalanceEntity.getOriginalPrice());
            goods.setPayPrice(trialBalanceEntity.getPayPrice());

            // 组队信息
            List<GoodsMarketResponseDTO.Team> teams = new ArrayList<>();
            if (null != userGroupBuyOrderDetailEntities && !userGroupBuyOrderDetailEntities.isEmpty()) {
                for (UserGroupBuyOrderDetailEntity userGroupBuyOrderDetailEntity : userGroupBuyOrderDetailEntities) {
                    GoodsMarketResponseDTO.Team team = GoodsMarketResponseDTO.Team.builder()
                            .userId(userGroupBuyOrderDetailEntity.getUserId())
                            .teamId(userGroupBuyOrderDetailEntity.getTeamId())
                            .activityId(userGroupBuyOrderDetailEntity.getActivityId())
                            .targetCount(userGroupBuyOrderDetailEntity.getTargetCount())
                            .completeCount(userGroupBuyOrderDetailEntity.getCompleteCount())
                            .lockCount(userGroupBuyOrderDetailEntity.getLockCount())
                            .validStartTime(userGroupBuyOrderDetailEntity.getValidStartTime())
                            .validEndTime(userGroupBuyOrderDetailEntity.getValidEndTime())
                            .validTimeCountdown(GoodsMarketResponseDTO.Team.differenceDateTime2Str(new Date(), userGroupBuyOrderDetailEntity.getValidEndTime()))
                            .outTradeNo(userGroupBuyOrderDetailEntity.getOutTradeNo())
                            .build();
                    teams.add(team);
                }
            }

            // 统计信息
            GoodsMarketResponseDTO.TeamStatistic teamStatistic = GoodsMarketResponseDTO.TeamStatistic.builder()
                    .allTeamCount(teamStatisticVO.getAllTeamCount())
                    .allTeamCompleteCount(teamStatisticVO.getAllTeamCompleteCount())
                    .allTeamUserCount(teamStatisticVO.getAllTeamUserCount())
                    .build();

            Response<GoodsMarketResponseDTO> response = Response.<GoodsMarketResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GoodsMarketResponseDTO.builder()
                            .activityId(activityId)
                            .goods(goods)
                            .teamList(teams)
                            .teamStatistic(teamStatistic)
                            .build())
                    .build();

            log.info("查询拼团营销配置完成:{} goodsId:{} response:{}", requestDTO.getUserId(), requestDTO.getGoodsId(), JSON.toJSONString(response));

            return response;
        } catch (Exception e) {
            log.error("查询拼团营销配置失败:{} goodsId:{}", requestDTO.getUserId(), requestDTO.getGoodsId(), e);
            return Response.<GoodsMarketResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    public Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfigFallBack(@RequestBody GoodsMarketRequestDTO requestDTO) {
        log.error("查询拼团营销配置限流:{}", requestDTO.getUserId());
        return Response.<GoodsMarketResponseDTO>builder()
                .code(ResponseCode.RATE_LIMITER.getCode())
                .info(ResponseCode.RATE_LIMITER.getInfo())
                .build();
    }
}
