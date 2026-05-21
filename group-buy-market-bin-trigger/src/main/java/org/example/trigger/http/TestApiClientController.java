package org.example.trigger.http;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.api.dto.NotifyRequestDTO;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName : TestApiClientController
 * @Description : 回调服务接口测试
 * @Author : Bingo
 * @Date: 2026/1/9  16:35
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/test/")
public class TestApiClientController {

    /**
     * 模拟回调案例
     *
     * @param notifyRequestDTO
     * @return success 成功，error 失败
     */
    @RequestMapping(value = "group_buy_notify", method = RequestMethod.POST)
    public String groupBuyNotify(@RequestBody NotifyRequestDTO notifyRequestDTO) {

        log.info("模拟测试第三方服务接收拼团回调 {}", JSON.toJSONString(notifyRequestDTO));

        return "success";

    }
}
