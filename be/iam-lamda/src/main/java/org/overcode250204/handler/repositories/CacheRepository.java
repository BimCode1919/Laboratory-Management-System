package org.overcode250204.handler.repositories;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheRepository {

    private final String tableName;

    private final DynamoDbClient dynamoDb;

    private final String fetchPrivilegesUrl;

    private final HttpClient httpClient;

    public CacheRepository(DynamoDbClient dynamoDbClient, String tableName, String fetchPrivilegesUrl, HttpClient httpClient) {
        this.tableName =  tableName;
        this.dynamoDb = dynamoDbClient;
        this.fetchPrivilegesUrl = fetchPrivilegesUrl;
        this.httpClient = httpClient;
    }

    public Map<String, Object> getPrivilegesByRole(String role, Context context) {
        try {
            GetItemRequest request = GetItemRequest.builder()
                    .tableName(tableName)
                    .key(Map.of("role", AttributeValue.fromS(role)))
                    .build();

            GetItemResponse response = dynamoDb.getItem(request);
            if (!response.hasItem()) {
                context.getLogger().log("Cache MISS for role " + role);
                return Map.of();
            }

            Map<String, AttributeValue> item = response.item();

            List<String> privileges = item.get("privileges").ss();

            long expireAt = Long.parseLong(item.get("expireAt").n());
            long now = Instant.now().getEpochSecond();

            if (expireAt < now) {
                context.getLogger().log("Cache expired for role " + role + "\n");
                return Map.of();
            }


            Map<String, Object> result = new HashMap<>();
            result.put("privileges", privileges);
            result.put("expireAt", expireAt);

            context.getLogger().log("Cache HIT for " + role);
            return result;
        } catch (Exception e) {
            context.getLogger().log("getPrivilegesByRole error: " + e.getMessage());
            return Map.of();
        }
    }

    public boolean isExpired(Map<String, Object> cache) {
        if (cache == null || cache.isEmpty() || cache.get("expireAt") == null) {
            return true;
        }
        long expireAt = ((Number) cache.get("expireAt")).longValue();
        long now = Instant.now().getEpochSecond();
        return now > expireAt;
    }

    public Map<String, Object> fetchPrivileges(String role, Context context) {
        try {
            String url = fetchPrivilegesUrl.replace("{role}", role);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return new ObjectMapper().readValue(response.body(), Map.class);
            } else {
                context.getLogger().log("IAM response not 200: " + response.statusCode() + "\n");
                return Map.of();
            }
        } catch (Exception e) {
            context.getLogger().log("Error fetching privileges: " + e.getMessage() + "\n");
            return Map.of();
        }

    }

    public void savePrivilegesToDynamo(String role, List<String> privileges, long ttlSeconds) {
        long now = System.currentTimeMillis();
        long ttl = Instant.now().getEpochSecond() + ttlSeconds;

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("role", AttributeValue.builder().s(role).build());
        item.put("createdAt", AttributeValue.builder().n(String.valueOf(now)).build());
        item.put("expireAt", AttributeValue.builder().n(String.valueOf(ttl)).build());

        if (privileges != null && !privileges.isEmpty()) {
            item.put("privileges", AttributeValue.builder().ss(privileges).build());
        }

        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build());
    }


}
