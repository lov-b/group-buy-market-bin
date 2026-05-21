package org.example.api;

import org.example.api.dto.GoodsMarketRequestDTO;
import org.example.api.dto.GoodsMarketResponseDTO;
import org.example.api.response.Response;

/**
 * @ClassName : IMarketIndexService
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/12  20:18
 */
public interface IMarketIndexService {

    /**
     * 查询拼团营销配置
     *
     * @param requestDTO
     * @return
     */
    Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(GoodsMarketRequestDTO requestDTO);
}
