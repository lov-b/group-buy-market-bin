package org.example.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName : OKHttpClientConfig
 * @Description : http 框架
 * @Author : Bingo
 * @Date: 2026/1/11  11:11
 */
@Configuration
public class OKHttpClientConfig {

    @Bean
    public OkHttpClient httpClient() {
        return new OkHttpClient();
    }
}
