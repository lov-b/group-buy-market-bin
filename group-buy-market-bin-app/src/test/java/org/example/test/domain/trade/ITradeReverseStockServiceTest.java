package org.example.test.domain.trade;

import com.alibaba.fastjson.JSON;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.api.IMarketTradeService;
import org.example.api.dto.LockMarketPayOrderRequestDTO;
import org.example.api.dto.LockMarketPayOrderResponseDTO;
import org.example.api.response.Response;
import org.example.domain.trade.model.entity.TradeRefundBehaviorEntity;
import org.example.domain.trade.model.entity.TradeRefundCommandEntity;
import org.example.domain.trade.service.ITradeRefundOrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

/**
 * @ClassName : ITradeReverseStockServiceTest
 * @Description :
 * @Author : Bingo
 * @Date: 2026/2/26  11:37
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ITradeReverseStockServiceTest {

    @Resource
    private ITradeRefundOrderService tradeRefundOrderService;

    @Resource
    private IMarketTradeService marketTradeService;


    @Test
    public void test_lockMarketPayOrder() {
        String teamId = null;

        for (int i = 1; i < 4; i++) {
            LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO = new LockMarketPayOrderRequestDTO();
            lockMarketPayOrderRequestDTO.setTeamId(teamId);
            lockMarketPayOrderRequestDTO.setUserId("bin100" + i);
            lockMarketPayOrderRequestDTO.setActivityId(100123L);
            lockMarketPayOrderRequestDTO.setGoodsId("9890001");
            lockMarketPayOrderRequestDTO.setSource("s01");
            lockMarketPayOrderRequestDTO.setChannel("c01");
            lockMarketPayOrderRequestDTO.setNotifyMQ();
            lockMarketPayOrderRequestDTO.setOutTradeNo(RandomStringUtils.randomNumeric(12));
            Response<LockMarketPayOrderResponseDTO> lockMarketPayOrderResponseDTO =
                    marketTradeService.lockMarketPayOrder(lockMarketPayOrderRequestDTO);

            teamId = lockMarketPayOrderResponseDTO.getData().getTeamId();
            log.info("第{}笔，测试结果 req:{} res:{}", i, JSON.toJSONString(lockMarketPayOrderRequestDTO), JSON.toJSONString(lockMarketPayOrderResponseDTO));
        }

    }

    @Test
    public void test_refundOrder() throws Exception {
        TradeRefundCommandEntity tradeRefundCommandEntity = TradeRefundCommandEntity.builder()
                .userId("bin1001")
                .outTradeNo("249949652213")
                .source("s01")
                .channel("c01")
                .build();

        TradeRefundBehaviorEntity tradeRefundBehaviorEntity = tradeRefundOrderService.refundOrder(tradeRefundCommandEntity);

        log.info("请求参数:{}", JSON.toJSONString(tradeRefundCommandEntity));
        log.info("测试结果:{}", JSON.toJSONString(tradeRefundBehaviorEntity));

        // 暂停，等待MQ消息。处理完后，手动关闭程序
        new CountDownLatch(1).await();
    }

    @Test
    public void test_lockMarketPayOrder_reverse() throws InterruptedException {
        LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO = new LockMarketPayOrderRequestDTO();
        lockMarketPayOrderRequestDTO.setUserId("bin1004");
        lockMarketPayOrderRequestDTO.setTeamId("02739251");
        lockMarketPayOrderRequestDTO.setActivityId(100123L);
        lockMarketPayOrderRequestDTO.setGoodsId("9890001");
        lockMarketPayOrderRequestDTO.setSource("s01");
        lockMarketPayOrderRequestDTO.setChannel("c01");
        lockMarketPayOrderRequestDTO.setNotifyMQ();
        lockMarketPayOrderRequestDTO.setOutTradeNo(RandomStringUtils.randomNumeric(12));

        Response<LockMarketPayOrderResponseDTO> lockMarketPayOrderResponseDTOResponse = marketTradeService.lockMarketPayOrder(lockMarketPayOrderRequestDTO);

        log.info("测试结果 req:{} res:{}", JSON.toJSONString(lockMarketPayOrderRequestDTO), JSON.toJSONString(lockMarketPayOrderResponseDTOResponse));
    }

}
