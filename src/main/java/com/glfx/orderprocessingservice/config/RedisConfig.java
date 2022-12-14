package com.glfx.orderprocessingservice.config;
import com.glfx.orderprocessingservice.DTO.LoggerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
public class RedisConfig {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Value("${topic.name:orderOperations}")
    private String topic;


    @Bean
    public ReactiveRedisOperations<String, Object> objectTemplate(LettuceConnectionFactory lettuceConnectionFactory){
        RedisSerializer<Object> valueSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext.<String, Object>newSerializationContext(RedisSerializer.string())
                .value(valueSerializer)
                .build();
        return new ReactiveRedisTemplate<String, Object>(lettuceConnectionFactory, serializationContext);
    }

    @Bean
    public ReactiveRedisOperations<String, LoggerEntity> loggerEntityTemplate(LettuceConnectionFactory lettuceConnectionFactory){
        RedisSerializer<LoggerEntity> valueSerializer = new Jackson2JsonRedisSerializer<>(LoggerEntity.class);
        RedisSerializationContext<String, LoggerEntity> serializationContext = RedisSerializationContext.<String, LoggerEntity>newSerializationContext(RedisSerializer.string())
                .value(valueSerializer)
                .build();
        return new ReactiveRedisTemplate<String, LoggerEntity>(lettuceConnectionFactory, serializationContext);
    }


}
