package com.example.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // bypass JWT filter for public endpoints
        if (path.startsWith("/api/v1/auth/")) {
            return chain.filter(exchange);
        }

        String correlationId = UUID.randomUUID().toString();

        exchange.getRequest().mutate()
                .header("X-Correlation-Id", correlationId)
                .build();

        log.info("REQ [{}] -> {}", correlationId, exchange.getRequest().getURI());

        return chain.filter(exchange)
                .doOnSuccess(v ->
                        log.info("RES [{}] -> {}", correlationId, exchange.getResponse().getStatusCode())
                );
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

