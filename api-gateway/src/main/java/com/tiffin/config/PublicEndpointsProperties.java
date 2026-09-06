package com.tiffin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "")
public class PublicEndpointsProperties {

    private List<String> publicEndpoints;
    private List<String> protectedEndpoints; // explicit overrides — checked before publicEndpoints

    public List<String> getPublicEndpoints() {
        return publicEndpoints != null ? publicEndpoints : Collections.emptyList();
    }

    public void setPublicEndpoints(List<String> publicEndpoints) {
        this.publicEndpoints = publicEndpoints;
    }

    public List<String> getProtectedEndpoints() {
        return protectedEndpoints != null ? protectedEndpoints : Collections.emptyList();
    }

    public void setProtectedEndpoints(List<String> protectedEndpoints) {
        this.protectedEndpoints = protectedEndpoints;
    }
}