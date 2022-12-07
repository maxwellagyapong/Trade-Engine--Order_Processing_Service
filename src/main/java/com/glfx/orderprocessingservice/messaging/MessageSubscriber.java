package com.glfx.orderprocessingservice.market;

import com.glfx.orderprocessingservice.DTO.MarketData;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class MessageSubscriber {
    @Autowired
    private ReactiveRedisOperations<String, MarketData[]> redisTemplate;

    @Value("${topic.name:order}")
    private String topic;

    @PostConstruct
    private void init(){
        this.redisTemplate
                .listenTo(ChannelTopic.of(topic))
                .map(ReactiveSubscription.Message::getMessage)
                .subscribe(System.out::println);
    }

}
