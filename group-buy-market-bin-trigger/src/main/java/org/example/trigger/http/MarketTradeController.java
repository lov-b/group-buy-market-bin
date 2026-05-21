package org.example.trigger.http;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.api.IMarketTradeService;
import org.example.api.dto.*;
import org.example.api.response.Response;
import org.example.domain.activity.model.entity.MarketProductEntity;
import org.example.domain.activity.model.entity.TrialBalanceEntity;
import org.example.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import org.example.domain.activity.service.IIndexGroupBuyMarketService;
import org.example.domain.trade.model.entity.*;
import org.example.domain.trade.model.valobj.GroupBuyProgressVO;
import org.example.domain.trade.model.valobj.NotifyConfigVO;
import org.example.domain.trade.model.valobj.NotifyTypeEnumVO;
import org.example.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import org.example.domain.trade.service.ITradeLockOrderService;
import org.example.domain.trade.service.ITradeRefundOrderService;
import org.example.domain.trade.service.ITradeSettlementOrderService;
import org.example.types.enums.ResponseCode;
import org.example.types.exception.AppException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName : MarketTradeController
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/6  12:25
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/trade/")
public class MarketTradeController implements IMarketTradeService {

    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;

    @Resource
    private ITradeLockOrderService tradeOrderService;

    @Resource
    private ITradeSettlementOrderService tradeSettlementOrderService;

    @Resource
    private ITradeRefundOrderService tradeRefundOrderService;

    @RequestMapping(value = "lock_market_pay_order", method = RequestMethod.POST)
    @Override
    public Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(@RequestBody LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO) {

        try {
            // 参数
            String userId = lockMarketPayOrderRequestDTO.getUserId();
            String source = lockMarketPayOrderRequestDTO.getSource();
            String channel = lockMarketPayOrderRequestDTO.getChannel();
            String goodsId = lockMarketPayOrderRequestDTO.getGoodsId();
            Long activityId = lockMarketPayOrderRequestDTO.getActivityId();
            String outTradeNo = lockMarketPayOrderRequestDTO.getOutTradeNo();
            String teamId = lockMarketPayOrderRequestDTO.getTeamId();
            LockMarketPayOrderRequestDTO.NotifyConfigVO notifyConfigVO = lockMarketPayOrderRequestDTO.getNotifyConfigVO();

            log.info("营销交易锁单:{} LockMarketPayOrderRequestDTO:{}", userId,
                    JSON.toJSONString(lockMarketPayOrderRequestDTO));

            if (StringUtils.isBlank(userId) || StringUtils.isBlank(source) ||
                    StringUtils.isBlank(channel) || StringUtils.isBlank(goodsId) ||
                    null == activityId || ("HTTP".equals(notifyConfigVO.getNotifyType()) && StringUtils.isBlank(notifyConfigVO.getNotifyUrl()))) {
                return Response.<LockMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            // 查询 outTradeNo 是否已经存在交易记录
            MarketPayOrderEntity marketPayOrderEntity = tradeOrderService.queryNoPayMarketPayOrderByOutTradeNo(userId,
                    outTradeNo);
            if (null != marketPayOrderEntity && TradeOrderStatusEnumVO.CREATE.equals(marketPayOrderEntity.getTradeOrderStatusEnumVO())) {
                LockMarketPayOrderResponseDTO lockMarketPayOrderResponseDTO = LockMarketPayOrderResponseDTO.builder()
                        .orderId(marketPayOrderEntity.getOrderId())
                        .originalPrice(marketPayOrderEntity.getOriginalPrice())
                        .deductionPrice(marketPayOrderEntity.getDeductionPrice())
                        .payPrice(marketPayOrderEntity.getPayPrice())
                        .tradeOrderStatus(marketPayOrderEntity.getTradeOrderStatusEnumVO().getCode())
                        .build();

                log.info("交易锁单记录(存在):{} marketPayOrderEntity:{}", userId,
                        JSON.toJSONString(marketPayOrderEntity));

                return Response.<LockMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info(ResponseCode.SUCCESS.getInfo())
                        .data(lockMarketPayOrderResponseDTO)
                        .build();
            }

            // 若有拼团id字段 则判断拼团锁单是否完成了目标
            if (null != teamId) {
                GroupBuyProgressVO groupBuyProgressVO = tradeOrderService.queryGroupBuyProgress(teamId);
                if (null != groupBuyProgressVO && Objects.equals(groupBuyProgressVO.getTargetCount(),
                        groupBuyProgressVO.getLockCount())) {
                    log.info("交易锁单拦截-拼单目标已达成:{} {}", userId, teamId);
                    return Response.<LockMarketPayOrderResponseDTO>builder()
                            .code(ResponseCode.E0006.getCode())
                            .info(ResponseCode.E0006.getInfo())
                            .build();
                }
            }

            // 营销优惠试算
            TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(
                    MarketProductEntity.builder()
                            .userId(userId)
                            .source(source)
                            .channel(channel)
                            .goodsId(goodsId)
                            .activityId(activityId)
                            .build());

            GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = trialBalanceEntity.getGroupBuyActivityDiscountVO();

            //锁单 创建入参对象
            UserEntity userEntity = UserEntity.builder()
                    .userId(userId)
                    .build();
            PayActivityEntity payActivityEntity = PayActivityEntity.builder()
                    .teamId(teamId)
                    .activityId(activityId)
                    .activityName(groupBuyActivityDiscountVO.getActivityName())
                    .targetCount(groupBuyActivityDiscountVO.getTarget())
                    .startTime(groupBuyActivityDiscountVO.getStartTime())
                    .endTime(groupBuyActivityDiscountVO.getEndTime())
                    .validTime(groupBuyActivityDiscountVO.getValidTime())
                    .build();
            PayDiscountEntity payDiscountEntity = PayDiscountEntity.builder()
                    .outTradeNo(outTradeNo)
                    .channel(channel)
                    .source(source)
                    .originalPrice(trialBalanceEntity.getOriginalPrice())
                    .deductionPrice(trialBalanceEntity.getDeductionPrice())
                    .payPrice(trialBalanceEntity.getPayPrice())
                    .goodsName(trialBalanceEntity.getGoodsName())
                    .goodsId(trialBalanceEntity.getGoodsId())
                    .notifyConfigVO(
                            // 构建回调通知对象
                            NotifyConfigVO.builder()
                                    .notifyType(NotifyTypeEnumVO.valueOf(notifyConfigVO.getNotifyType()))
                                    .notifyMQ(notifyConfigVO.getNotifyMQ())
                                    .notifyUrl(notifyConfigVO.getNotifyUrl())
                                    .build())       //  notifyUrl放到活动下，因为同个渠道的活动回调地址都一致
                    .build();
            log.info("活动实体：{}", JSON.toJSONString(payActivityEntity));
            marketPayOrderEntity = tradeOrderService.lockMarketPayOrder(userEntity, payActivityEntity,
                    payDiscountEntity);
            log.info("交易锁单记录(新):{} marketPayOrderEntity:{}", userId, JSON.toJSONString(marketPayOrderEntity));

            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(LockMarketPayOrderResponseDTO.builder()
                            .teamId(marketPayOrderEntity.getTeamId())
                            .orderId(marketPayOrderEntity.getOrderId())
                            .deductionPrice(marketPayOrderEntity.getDeductionPrice())
                            .payPrice(marketPayOrderEntity.getPayPrice())
                            .originalPrice(marketPayOrderEntity.getOriginalPrice())
                            .tradeOrderStatus(marketPayOrderEntity.getTradeOrderStatusEnumVO().getCode())
                            .build())
                    .build();


        } catch (AppException e) {
            log.error("营销交易锁单业务异常:{} LockMarketPayOrderRequestDTO:{}",
                    lockMarketPayOrderRequestDTO.getUserId(), JSON.toJSONString(lockMarketPayOrderRequestDTO), e);
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("营销交易锁单服务失败:{} LockMarketPayOrderRequestDTO:{}",
                    lockMarketPayOrderRequestDTO.getUserId(), JSON.toJSONString(lockMarketPayOrderRequestDTO), e);
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "settlement_market_pay_order", method = RequestMethod.POST)
    @Override
    public Response<SettlementMarketPayOrderResponseDTO> settlementMarketPayOrder(@RequestBody SettlementMarketPayOrderRequestDTO requestDTO) {

        try {
            log.info("营销交易组队结算开始:{} outTradeNo:{}", requestDTO.getUserId(), requestDTO.getOutTradeNo());

            if (StringUtils.isBlank(requestDTO.getUserId()) || StringUtils.isBlank(requestDTO.getSource()) ||
                    StringUtils.isBlank(requestDTO.getChannel()) || StringUtils.isBlank(requestDTO.getOutTradeNo()) ||
                    null == requestDTO.getOutTradeTime()) {
                log.info("settlementMarketPayOrder - isBlank - ILLEGAL_PARAMETER");
                log.info("settlementMarketPayOrder - requestDTO:{}", JSON.toJSONString(requestDTO));
                return Response.<SettlementMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            //1.结算服务
            TradePaySuccessEntity tradePaySuccessEntity = TradePaySuccessEntity.builder()
                    .source(requestDTO.getSource())
                    .channel(requestDTO.getChannel())
                    .userId(requestDTO.getUserId())
                    .outTradeNo(requestDTO.getOutTradeNo())
                    .outTradeTime(requestDTO.getOutTradeTime())
                    .build();
            TradePaySettlementEntity tradePaySettlementEntity = tradeSettlementOrderService.settlementMarketPayOrder(tradePaySuccessEntity);
            log.info("tradePaySettlementEntity - {}", JSON.toJSONString(tradePaySettlementEntity));
            SettlementMarketPayOrderResponseDTO responseDTO = SettlementMarketPayOrderResponseDTO.builder()
                    .userId(tradePaySettlementEntity.getUserId())
                    .teamId(tradePaySettlementEntity.getTeamId())
                    .activityId(tradePaySettlementEntity.getActivityId())
                    .outTradeNo(tradePaySettlementEntity.getOutTradeNo())
                    .build();

            //2.返回结果
            Response<SettlementMarketPayOrderResponseDTO> response = new Response<>();
            response.setCode(ResponseCode.SUCCESS.getCode());
            response.setInfo(ResponseCode.SUCCESS.getInfo());
            response.setData(responseDTO);
            log.info("营销交易组队结算完成:{} outTradeNo:{} response:{}", requestDTO.getUserId(), requestDTO.getOutTradeNo(), JSON.toJSONString(response));

            return response;

        } catch (AppException e) {
            log.error("营销交易组队结算异常:{} SettlementMarketPayOrderRequestDTO:{}", requestDTO.getUserId(), JSON.toJSONString(requestDTO), e);
            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("营销交易组队结算失败:{} SettlementMarketPayOrderRequestDTO:{}", requestDTO.getUserId(), JSON.toJSONString(requestDTO), e);
            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "refund_market_pay_order", method = RequestMethod.POST)
    @Override
    public Response<RefundMarketPayOrderResponseDTO> refundMarketPayOrder(@RequestBody RefundMarketPayOrderRequestDTO requestDTO) throws Exception {

        try {
            log.info("营销拼团退单开始:{} outTradeNo:{}", requestDTO.getUserId(), requestDTO.getOutTradeNo());

            if (StringUtils.isBlank(requestDTO.getUserId()) || StringUtils.isBlank(requestDTO.getOutTradeNo()) || StringUtils.isBlank(requestDTO.getSource()) || StringUtils.isBlank(requestDTO.getChannel())) {
                return Response.<RefundMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            TradeRefundBehaviorEntity tradeRefundBehaviorEntity = tradeRefundOrderService.refundOrder(TradeRefundCommandEntity.builder()
                    .userId(requestDTO.getUserId())
                    .outTradeNo(requestDTO.getOutTradeNo())
                    .channel(requestDTO.getChannel())
                    .source(requestDTO.getSource())
                    .build());

            RefundMarketPayOrderResponseDTO responseDTO = new RefundMarketPayOrderResponseDTO();
            responseDTO.setUserId(tradeRefundBehaviorEntity.getUserId());
            responseDTO.setOrderId(tradeRefundBehaviorEntity.getOrderId());
            responseDTO.setTeamId(tradeRefundBehaviorEntity.getTeamId());
            responseDTO.setCode(tradeRefundBehaviorEntity.getTradeRefundBehaviorEnum().getCode());
            responseDTO.setInfo(tradeRefundBehaviorEntity.getTradeRefundBehaviorEnum().getInfo());

            Response<RefundMarketPayOrderResponseDTO> response = Response.<RefundMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(responseDTO)
                    .build();

            log.info("营销拼团退单完成:{} outTradeNo:{} response:{}", requestDTO.getUserId(), requestDTO.getOutTradeNo(), JSON.toJSONString(response));

            return response;
        } catch (AppException e) {
            log.error("营销拼团退单异常:{} RefundMarketPayOrderRequestDTO:{}", requestDTO.getUserId(), JSON.toJSONString(requestDTO), e);
            return Response.<RefundMarketPayOrderResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("营销拼团退单失败:{} RefundMarketPayOrderRequestDTO:{}", requestDTO.getUserId(), JSON.toJSONString(requestDTO), e);
            return Response.<RefundMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
