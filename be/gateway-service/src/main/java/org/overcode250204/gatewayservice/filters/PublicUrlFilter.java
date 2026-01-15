package org.overcode250204.gatewayservice.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class PublicUrlFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/iam-service/login",
            "/iam-service/first-login/change-password",
            "/iam-service/forgot-password",
            "/iam-service/forgot-password-confirm",
            "/iam-service/refresh-token",
            "/swagger-ui",
            "/swagger-resources",
            "/webjars",
            "/configuration",
            "/iam-service/callback/google",
            "/iam-service/verify"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith) || path.endsWith("privileges") || path.endsWith("/v3/api-docs") || path.endsWith("google");

        if (isPublic) {
            exchange.getAttributes().put("isPublicRoute", true);
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
