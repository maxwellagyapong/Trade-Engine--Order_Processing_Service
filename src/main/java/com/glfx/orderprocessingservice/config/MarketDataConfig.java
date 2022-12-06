package com.glfx.orderprocessingservice.config;

import com.glfx.orderprocessingservice.market.marketRepository.MarketDataRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



//Creating a bean to store market data received from the exchanges
@Configuration
public class MarketDataConfig {

    @Bean
    public MarketDataRepo getMarketDataRepo(){
        return new MarketDataRepo();
    }

}
