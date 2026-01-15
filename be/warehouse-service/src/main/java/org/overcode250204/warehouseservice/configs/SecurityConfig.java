package org.overcode250204.warehouseservice.configs;




import lombok.RequiredArgsConstructor;
import org.overcode250204.warehouseservice.exceptions.CustomAccessDeniedHandler;
import org.overcode250204.warehouseservice.exceptions.CustomAuthenticationEntryPoint;
import org.overcode250204.warehouseservice.filters.AuditLoggingFilter;
import org.overcode250204.warehouseservice.filters.GatewayHeaderAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
//            "/configuration/**",
            "/actuator/health",
            "/actuator/info",
            "/actuator/**",
    };

    @Bean
    public GatewayHeaderAuthFilter gatewayHeaderAuthFilter() {
        return new GatewayHeaderAuthFilter();
    }

    @Bean
    public AuditLoggingFilter auditLoggingFilter() {
        return new AuditLoggingFilter();
    }

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, GatewayHeaderAuthFilter gatewayHeaderAuthFilter, AuditLoggingFilter auditLoggingFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(PATH).permitAll()
                        .requestMatchers("/login/**", "/users/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(gatewayHeaderAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(auditLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .build();



    }



}
