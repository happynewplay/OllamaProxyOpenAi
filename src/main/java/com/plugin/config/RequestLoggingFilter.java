package com.plugin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import java.util.Map;

//@Configuration
public class RequestLoggingFilter implements WebFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 打印基本请求信息
        logger.info("Request Path: {}", request.getPath());
        logger.info("Request Method: {}", request.getMethod());
        logger.info("Request URI: {}", request.getURI());

        // 打印请求头
        request.getHeaders().forEach((key, value) ->
                logger.info("Header - {}: {}", key, value)
        );

        // 处理GET请求参数
        Map<String, String> queryParams = request.getQueryParams().toSingleValueMap();
        if (!queryParams.isEmpty()) {
            logger.info("GET Parameters:");
            queryParams.forEach((key, value) ->
                    logger.info("  {}: {}", key, value)
            );
        }

        // 处理POST请求体（需要特殊处理）
        return DataBufferUtils.join(request.getBody())
                .flatMap(dataBuffer -> {
                    // 将dataBuffer转换为可重复读取的字节数组
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    // 转换为字符串
                    String body = new String(bytes);
                    logger.info("Request Body: {}", body);

                    // 创建一个新的请求体，使其可重复读取
                    Flux<DataBuffer> cachedBody = Flux.defer(() -> {
                        DataBuffer buffer = new DefaultDataBufferFactory().wrap(bytes);
                        return Mono.just(buffer);
                    });

                    // 创建一个新的ServerWebExchange，替换请求体
                    ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(request) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return cachedBody;
                        }
                    };

                    ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

                    return chain.filter(mutatedExchange);
                });
    }

    @Override
    public int getOrder() {
        // 设置过滤器优先级
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
