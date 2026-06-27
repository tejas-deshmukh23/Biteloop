package com.tiffin.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * HTTP client for calling user-service internal endpoints.
 *
 * TODO: Replace with Kafka event in Milestone 8
 * PROVIDER_REGISTERED event → user-service consumer updates providerId
 * Current approach creates temporary coupling — acceptable for MVP
 */
@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserServiceClient(
            @Value("${services.user-service.url:http://user-service:8081}") 
            String userServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.userServiceUrl = userServiceUrl;
    }

    /**
     * Calls user-service to store providerId against the user account.
     * This allows JWT token to include providerId for PROVIDER role users.
     * Fire and forget — if it fails, log the error but don't fail registration.
     */
    public void updateProviderId(String userId, String providerId) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(userServiceUrl)
                    .path("/api/users/internal/update-provider-id")
                    .queryParam("userId", userId)
                    .queryParam("providerId", providerId)
                    .toUriString();
            
            log.info("url which we are hitting is : ",url);

            restTemplate.put(url, null);
            log.info("Successfully updated providerId={} for userId={}", 
                    providerId, userId);

        } catch (Exception e) {
            // Don't fail provider registration if this call fails
            // Provider can still register — providerId sync is eventual
            log.error("Failed to update providerId in user-service: {}", 
                    e.getMessage());
        }
    }
}