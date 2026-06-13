package com.tiffin.service;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.tiffin.dto.MenuItemResponse;

/*
All Redis operations for menu-service lives here.
Business logic (MenuService) calls this - stays clean.

Cache key pattern: "menu:provider:{providerId}"
Stores the public-facing list (available items only).
TTL: 10 minutes - menus don't change every second.

We only cache the public listig (available items).
provider's own full listing (includes unavailable) is not cached
because it changes more frequently and is low traffic.
*/

@Service
public class MenuCacheService {
	private static final String KEY_PREFIX = "menu:provider:";
	private static final Duration TTL = Duration.ofMinutes(10);
	
	private final RedisTemplate<String, Object> redisTemplate;
	
	public MenuCacheService(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
	
	private String buildKey(String providerId) {
        return KEY_PREFIX + providerId;
    }

    /**
     * Store the available menu list for a provider.
     * Called after fetching from Postgres on a cache miss.
     */
	
	@SuppressWarnings("unchecked")
    public void put(String providerId, List<MenuItemResponse> items) {
		try {
	        redisTemplate.opsForValue().set(buildKey(providerId), items, TTL);
	        System.out.println("Cache written for key: " + buildKey(providerId));
	    } catch (Exception e) {
	        System.err.println("Redis write failed: " + e.getMessage());
	    }
    }
	
	/**
     * Fetch cached menu for a provider.
     * Returns null on cache miss — caller handles the miss.
     */
    @SuppressWarnings("unchecked")
    public List<MenuItemResponse> get(String providerId) {
    	try {
    		Object cached = redisTemplate.opsForValue().get(buildKey(providerId));
            if (cached == null) return null;
            return (List<MenuItemResponse>) cached;
    	}catch (Exception e) {
            System.err.println(">>> Redis GET failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        
    }

    /**
     * Invalidate cache for a provider.
     * Called on every create, update, toggle, or soft-delete.
     * Next GET will rebuild from Postgres.
     */
    public void evict(String providerId) {
        redisTemplate.delete(buildKey(providerId));
    }
}
