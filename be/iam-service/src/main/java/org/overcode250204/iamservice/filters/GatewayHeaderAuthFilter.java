package org.overcode250204.iamservice.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.iamservice.exceptions.ErrorCode;
import org.overcode250204.iamservice.exceptions.IamServiceException;
import org.overcode250204.utils.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
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

        if (path.endsWith("/login")
                || path.endsWith("/privileges")
                || isPublicPath(path)
                || path.startsWith("/first-login/")
                || path.startsWith("/get-list/")
                || path.startsWith("/forgot-password")
                || path.startsWith("/forgot-password-confirm")
                || path.startsWith("/callback/google")
                || path.startsWith("/verify")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        String data = username + "|" + groups + "|" + privileges;
        String expectedSignature = HmacUtils.hmacSha256(data, hmacSecret);
        if (!expectedSignature.equals(signature)) {
            log.warn("Invalid signature for username {}", username);
            throw new IamServiceException(ErrorCode.GATEWAY_SIGNATURE_INVALID);
        }



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

        UsernamePasswordAuthenticationToken authorization = new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authorization);
        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/swagger-ui")
                || path.contains("/v3/api-docs")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars")
                || path.startsWith("/configuration")
                || path.startsWith("/first-login/change-password")
                || path.startsWith("/refresh-token")
                || path.startsWith("/actuator")
                || path.startsWith("/callback/google");
    }
}
