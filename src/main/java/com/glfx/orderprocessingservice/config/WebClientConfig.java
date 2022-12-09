package com.glfx.orderprocessingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

public class WebClientConfig {

    @Bean
    public WebClient.Builder createWebClient(){
        return WebClient.builder();
    }
}
