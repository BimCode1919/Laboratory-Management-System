package org.overcode250204.monitoringservice.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.overcode250204.common.grpc.CognitoSub;
import org.overcode250204.common.grpc.IamServiceGrpc;
import org.overcode250204.monitoringservice.exceptions.ErrorCode;
import org.overcode250204.monitoringservice.exceptions.MonitoringException;
import org.overcode250204.utils.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class GatewayHeaderAuthFilter extends OncePerRequestFilter {

    @Value("${hmac-secret}")
    private String hmacSecret;

    @GrpcClient("iam-service")
    private IamServiceGrpc.IamServiceBlockingStub iamStub;

    private final static String HEADER_USER_NAME = "X-User-Name";
    private final static String HEADER_GROUPS = "X-Groups";
    private final static String HEADER_PRIVILEGES = "X-Privileges";
    private final static String HEADER_SIGNATURE_GATEWAY = "X-Signature";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String username = request.getHeader(HEADER_USER_NAME);
        String groups = request.getHeader(HEADER_GROUPS);
        String privileges = request.getHeader(HEADER_PRIVILEGES);
        String signature = request.getHeader(HEADER_SIGNATURE_GATEWAY);



        String path = request.getRequestURI();

        if (path.endsWith("/login") || path.startsWith("/users/") || isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String data = username + "|" + groups + "|" + privileges;
        String expectedSignature = HmacUtils.hmacSha256(data, hmacSecret);
        if (!expectedSignature.equals(signature)) {
            log.warn("Invalid signature for username {}", username);
            throw new MonitoringException(ErrorCode.GATEWAY_SIGNATURE_INVALID);
        }

        String userId;
        try {
            userId = iamStub.getUserByCognitoSub(
                    CognitoSub.newBuilder().setCognitoSub(username).build()
            ).getUserId();
        } catch (Exception e) {
            throw new MonitoringException(ErrorCode.GET_USER_ID_FROM_IAM_SERVICE_ERROR);
        }
        System.out.println(userId);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (groups != null && !groups.isBlank()) {
            authorities.addAll(Arrays.stream(groups.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(granted -> new SimpleGrantedAuthority("ROLE_" + granted))
                    .collect(Collectors.toList()));

        }

        if (privileges != null && !privileges.isBlank()) {
            authorities.addAll(Arrays.stream(privileges.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }

        UsernamePasswordAuthenticationToken authorization = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authorization);
        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/swagger-ui")
                || path.contains("/v3/api-docs")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars")
                || path.startsWith("/configuration")
                || path.startsWith("/actuator");
    }
}
