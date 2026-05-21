package org.example.infrastructure.gateway;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.example.types.enums.ResponseCode;
import org.example.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassName : GroupBuyNotifyService
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/11  11:19
 */
@Slf4j
@Service
public class GroupBuyNotifyService {

    @Resource
    private OkHttpClient okHttpClient;

    public String groupBuyNotify(String apiUrl, String notifyRequestDTOJson) throws Exception {
        try {
            /**
             * 这部分代码来自于apiPost中的代码生成功能
             * apipost生成的代码中有多个addHeader，只保留这一个就可以
             */
            // 1. 构建参数
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, notifyRequestDTOJson);
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .addHeader("content-type", "application/json")
                    .build();

            // 2. 调用接口
            Response response = okHttpClient.newCall(request).execute();

            return response.body().string();
        } catch (Exception e) {
            log.error("拼团回调 HTTP 接口服务异常 {}", apiUrl, e);
            throw new AppException(ResponseCode.HTTP_EXCEPTION);
        }
    }

}
