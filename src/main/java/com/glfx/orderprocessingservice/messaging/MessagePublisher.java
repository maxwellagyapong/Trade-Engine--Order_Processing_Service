package com.glfx.orderprocessingservice.messaging;

import com.glfx.orderprocessingservice.DTO.LoggerEntity;
import com.glfx.orderprocessingservice.utils.EntityTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class MessagePublisher {

    @Autowired
    private ReactiveRedisOperations<String, LoggerEntity> redisTemplate;

    @Value("${topic.name:orderOperations}")
    private String topic;


    public void makeLogMessage(String message){
        LoggerEntity newMessage = LoggerEntity.builder()
                .message(message)
                .logEntity(EntityTypes.ORDER.toString())
                .dateLogged(new Date())
                .build();


        publish(newMessage);
    }

    public void publish(LoggerEntity loggerMessage){
        this.redisTemplate.convertAndSend(topic,loggerMessage).subscribe();
    }

}