package com.glfx.orderprocessingservice.config;

import com.glfx.orderprocessingservice.DAOs.PortfolioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortfolioConfig {

    @Bean
    public PortfolioRepository getPortfolioDatRepo(){
            return new PortfolioRepository();
        }

}
