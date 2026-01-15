package org.overcode250204.testorderservice.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class AuditLoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long ms = System.currentTimeMillis() - startTime;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String user = (authentication != null) ? authentication.getName() : "anonymous";
            String authorities = (authentication != null) ? authentication.getAuthorities().toString() : "[]";
            log.info("[AUDIT] user={} method={} uri={} status={} time={}ms auths={}",
                    user, request.getMethod(), request.getRequestURI(), response.getStatus(), ms, authorities);
        }

    }
}
