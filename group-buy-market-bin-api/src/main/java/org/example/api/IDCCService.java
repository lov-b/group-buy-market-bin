package org.example.api;

import org.example.api.response.Response;

/**
 * @ClassName : IDCCService
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/3  21:06
 */
public interface IDCCService {

    Response<Boolean> updateConfig(String key, String value);

}
