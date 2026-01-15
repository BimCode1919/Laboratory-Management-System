package org.overcode250204.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class IamClient {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient httpClient;

    private final String fetchPrivilegesUrl;

    public IamClient(HttpClient httpClient, String fetchPrivilegesUrl) {
        this.httpClient = httpClient;
        this.fetchPrivilegesUrl = fetchPrivilegesUrl;
    }

    public Map<String, Object> fetchPrivileges(String username) {
        try {
            String url = fetchPrivilegesUrl.replace("{username}", username);
            log.info("Fetching privileges for url={}", url);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Map<String, Object> data = mapper.readValue(response.body(), Map.class);
                log.info("IAM returned: {}", data);
                return data;
            } else {
                log.error("IAM returned status {} for user {}: {}", response.statusCode(), username, response.body());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.emptyMap();
    }





}
