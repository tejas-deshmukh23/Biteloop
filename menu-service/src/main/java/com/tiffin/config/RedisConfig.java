package com.tiffin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Configures how Java objects are stored in Redis.
 *
 * Redis stores everything as bytes. We need to tell it:
 *   - Keys   → plain String (human-readable: "menu:provider:prv_abc123")
 *   - Values → JSON (so we can read them in Redis CLI if needed)
 *
 * JavaTimeModule is required because LocalDateTime is not
 * serializable by default in Jackson.
 */

@Configuration
public class RedisConfig {
	
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory){
		
		RedisTemplate<String, Object> template = new RedisTemplate();
		template.setConnectionFactory(factory);
		
		// Keys stored as plain strings
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
     // Values stored as JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());           // handles LocalDateTime
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO format, not array

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(mapper);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
		
	}

}
