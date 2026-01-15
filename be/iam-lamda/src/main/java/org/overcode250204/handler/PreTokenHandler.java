package org.overcode250204.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.overcode250204.handler.repositories.CacheRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;


public class PreTokenHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final HttpClient httpClient ;

    private final String fetchPrivilegesUrl;

    private final String tableName;

    private final int timeCachePrivileges;

    private final DynamoDbClient  dynamoDbClient;

    private final CacheRepository cacheRepository;

    public PreTokenHandler() {
        this.httpClient = HttpClient.newBuilder().build();
        this.timeCachePrivileges = System.getenv("TIME_CACHE_PRIVILEGES") != null ? Integer.parseInt(System.getenv("TIME_CACHE_PRIVILEGES")) : 3600;
        this.fetchPrivilegesUrl = System.getenv("FETCH_PRIVILEGES_URL");
        this.tableName = System.getenv("CACHE_TABLE");
        this.dynamoDbClient = DynamoDbClient.create();
        this.cacheRepository = new CacheRepository(dynamoDbClient, tableName, fetchPrivilegesUrl, httpClient);
    }


    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        context.getLogger().log(event.toString());

        List<String> allPrivileges = new ArrayList<>();
        try {

            Map<String, Object> request = (Map<String, Object>) event.get("request");
            Map<String, Object> groupConfig = (Map<String, Object>) request.get("groupConfiguration");
            List<String> groups = (List<String>) groupConfig.get("groupsToOverride");

            if (groups == null || groups.isEmpty()) {
                context.getLogger().log("No Cognito groups found " + groups);
                return event;
            }

            context.getLogger().log("User groups: " + groups);

            context.getLogger().log("Time Cache Prvileges: " + timeCachePrivileges);
            for (String role : groups) {
                Map<String, Object> cache = cacheRepository.getPrivilegesByRole(role, context);
                context.getLogger().log("Cache: " + cache + "Role: " + role);
                List<String> privileges;
                if (cache != null && !cacheRepository.isExpired(cache)) {
                    privileges = (List<String>) cache.get("privileges");
                    context.getLogger().log("Cache HIT for role " + role + ": " + privileges + "\n");
                } else {
                    context.getLogger().log("Cache MISS for role " + role + ", fetching from IAM Service...\n");
                    Map<String, Object> data = cacheRepository.fetchPrivileges(role, context);
                    context.getLogger().log("Cache HIT for role " + role + ": " + data + "\n");
                    Map<String, Object> getData = (Map<String, Object>) data.get("data");
                    privileges = (List<String>) getData.getOrDefault("privileges", List.of());
                    context.getLogger().log("Cache HIT for role " + role + ": " + data + "\n");
                    cacheRepository.savePrivilegesToDynamo(role, privileges, timeCachePrivileges);
                }
                allPrivileges.addAll(privileges);
            }

        } catch (Exception e) {
            context.getLogger().log(e.getMessage());
            throw new RuntimeException(e);
        }
        context.getLogger().log("All Privileges = " + allPrivileges);

        Map<String, Object> response = (Map<String, Object>) event.computeIfAbsent("response", k -> new HashMap<>());

        context.getLogger().log("response = " + response);

        Map<String, Object> accessTokenClaims = new HashMap<>();

        accessTokenClaims.put("privileges", allPrivileges);

        Map<String, Object> accessTokenGeneration = new HashMap<>();
        accessTokenGeneration.put("claimsToAddOrOverride", accessTokenClaims);

        Map<String, Object> claimsAndScopeOverrideDetails = new HashMap<>();
        claimsAndScopeOverrideDetails.put("accessTokenGeneration", accessTokenGeneration);

        response.put("claimsAndScopeOverrideDetails", claimsAndScopeOverrideDetails);

        event.put("response", response);

        context.getLogger().log("responseLast = " + response);
        context.getLogger().log("lastEvent = " + event);
        return event;
    }




}
