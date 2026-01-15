package org.overcode250204.iamservice.configs;

import lombok.RequiredArgsConstructor;
import org.overcode250204.iamservice.exceptions.CustomAccessDeniedHandler;
import org.overcode250204.iamservice.exceptions.CustomAuthenticationEntryPoint;
import org.overcode250204.iamservice.filters.AuditLoggingFilter;
import org.overcode250204.iamservice.filters.GatewayHeaderAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final static String[] PATH = {
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/configuration/**",
            "/actuator/health",
            "/actuator/info",
            "/first-login/change-password",
            "/get-list/user",
            "/forgot-password",
            "/forgot-password-confirm",
            "/refresh-token",
            "/actuator/**",
            "/callback/google",
            "/verify"
    };

    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public GatewayHeaderAuthFilter gatewayHeaderAuthFilter() {
        return new GatewayHeaderAuthFilter();
    }

    @Bean
    public AuditLoggingFilter auditLoggingFilter() {
        return new AuditLoggingFilter();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, GatewayHeaderAuthFilter gatewayHeaderAuthFilter, AuditLoggingFilter auditLoggingFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .logout(logout -> logout.disable())
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(PATH).permitAll()
                        .requestMatchers("/login/**", "/users/**", "/first-login/**", "/refresh-token", "/callback/google").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(gatewayHeaderAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(auditLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }



}
