package org.overcode250204.iamservice.configs;

import org.overcode250204.clients.IamClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class IamClientConfig {
    @Value("${fetch-privileges-url}")
    private String fetchPrivilegesUrl;

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Bean
    public IamClient iamClient(HttpClient httpClient) {
        return new IamClient(httpClient, fetchPrivilegesUrl);
    }
}
