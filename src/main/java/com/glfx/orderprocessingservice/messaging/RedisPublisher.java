package com.glfx.orderprocessingservice.messaging;

import com.glfx.orderprocessingservice.DTO.LoggerEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

public class RedisPublisher {
    @Bean
    public ReactiveRedisOperations<String, LoggerEntity> loggerEntityTemplate(LettuceConnectionFactory lettuceConnectionFactory){
        RedisSerializer<LoggerEntity> latestOrderRedisSerializer = new Jackson2JsonRedisSerializer<LoggerEntity>(LoggerEntity.class);
        RedisSerializationContext<String, LoggerEntity> serializationContext = RedisSerializationContext.<String, LoggerEntity>newSerializationContext(
                RedisSerializer.string()
        ).value(latestOrderRedisSerializer).build();

        return new ReactiveRedisTemplate<String, LoggerEntity>(lettuceConnectionFactory, serializationContext);
    }
}
