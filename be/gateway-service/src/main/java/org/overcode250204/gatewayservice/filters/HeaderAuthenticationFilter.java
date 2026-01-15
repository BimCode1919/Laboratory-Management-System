package org.overcode250204.gatewayservice.filters;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.gatewayservice.services.JwtService;
import org.overcode250204.utils.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeaderAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    @Value("${hmac-secret}")
    private String hmacSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Boolean isPublic = exchange.getAttribute("isPublicRoute");
        if (Boolean.TRUE.equals(isPublic)) {
            log.info("Public route matched");
            return chain.filter(exchange)
                    .doOnSuccess(aVoid -> log.info("Filter chain finished for"))
                    .doOnError(e -> log.error("Filter chain error", e));
        }

        ServerHttpRequest request = exchange.getRequest();
        String authorizationHeader = request.getHeaders().getFirst("Authorization");
        if (authorizationHeader == null ||  !authorizationHeader.startsWith("Bearer ")) {
            return Mono.error(new BadCredentialsException("Missing or invalid Authorization header"));
        }

        String token  = authorizationHeader.substring(7);

        final JWTClaimsSet claims;

        try {
            claims = jwtService.verifyToken(token);
            if (claims == null) {
                return Mono.error(new BadCredentialsException("Invalid or expired JWT token"));
            }
        } catch (Exception e) {
            return Mono.error(new BadCredentialsException("Invalid or expired JWT token"));
        }

        Map<String, Object> value = claims.getClaims();

        List<String> groups = ( List<String>) claims.getClaims().get("cognito:groups");
        if (groups == null) {
            return Mono.error(new BadCredentialsException("Groups is null"));
        }
        List<String> privileges = (List<String>) claims.getClaims().get("privileges");
        if (privileges == null) return Mono.error(new BadCredentialsException("Privileges is null"));
        String username = value.get("sub").toString();
        String groupsCsv = String.join(",", groups);
        String privilegesCsv = String.join(",", privileges);

        String data = username + "|" +  groupsCsv + "|" + privilegesCsv;
        String signature = HmacUtils.hmacSha256(data, hmacSecret);

        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Name", username)
                .header("X-Groups", String.join(",", groups))
                .header("X-Privileges", String.join(",", privileges))
                .header("X-Signature", signature)
                .build();

        log.info(request.getHeaders().toString());
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }

}
