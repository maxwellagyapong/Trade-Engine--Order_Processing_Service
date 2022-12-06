package com.glfx.orderprocessingservice.market.marketDTO;
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

public class RedisConfig {
    @EnableScheduling
    @Configuration
    public class RedisConfiguration {

        @Autowired
        private RedisConnectionFactory redisConnectionFactory;

        @Value("${topic.name:order}")
        private String topic;

        @Bean
        public ReactiveRedisOperations<String, MarketData[]> objectTemplate(LettuceConnectionFactory lettuceConnectionFactory){
            RedisSerializer<MarketData[]> valueSerializer = new Jackson2JsonRedisSerializer<>(MarketData[].class);
            RedisSerializationContext<String, MarketData[]> serializationContext = RedisSerializationContext.<String, MarketData[]>newSerializationContext(RedisSerializer.string())
                    .value(valueSerializer)
                    .build();
            return new ReactiveRedisTemplate<String, MarketData[]>(lettuceConnectionFactory, serializationContext);
        }

    }

}
