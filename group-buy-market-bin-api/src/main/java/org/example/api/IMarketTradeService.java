package org.example.api;

import org.example.api.dto.*;
import org.example.api.response.Response;

/**
 * @ClassName : IMarketTradeService
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/6  12:23
 */
public interface IMarketTradeService {

    /**
     * 营销锁单
     *
     * @param lockMarketPayOrderRequestDTO
     * @return
     */
    Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO);

    /**
     * 营销结算
     *
     * @param requestDTO
     * @return
     */
    Response<SettlementMarketPayOrderResponseDTO> settlementMarketPayOrder(SettlementMarketPayOrderRequestDTO requestDTO);

    Response<RefundMarketPayOrderResponseDTO> refundMarketPayOrder(RefundMarketPayOrderRequestDTO requestDTO) throws Exception;

}
