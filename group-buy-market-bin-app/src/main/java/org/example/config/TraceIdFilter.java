package org.example.config;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * @ClassName : TraceIdFilter
 * @Description :
 * @Author : Bingo
 * @Date: 2026/2/5  16:23
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "trace-id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String traceId = UUID.randomUUID().toString();  //  随机生成一个定位请求的唯一id
            MDC.put(TRACE_ID, traceId); // 将traceId压入当前线程的map中，MDC.put方法会自动放到当前线程的map中
            filterChain.doFilter(request, response);    // 继续执行后续的请求业务
        } finally {
            MDC.clear();    // 清空当前线程的map，否则后续请求拿到当前线程后，会继续使用之前用过的map，导致traceId复用，无法定位日志。
        }
    }
}
