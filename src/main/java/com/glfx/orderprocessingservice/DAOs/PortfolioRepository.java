package com.glfx.orderprocessingservice.DAOs;

import com.glfx.orderprocessingservice.DTO.Portfolio;
import com.glfx.orderprocessingservice.DTO.PortfolioStock;
import com.glfx.orderprocessingservice.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;

public class PortfolioRepository {
    @Autowired
    private WebClient.Builder webclient;

    public Portfolio getClientPortfolioFromClientService(Order order){

        Portfolio clientPortfolio = webclient.build()
                .get()
                .uri("https://2a4c-102-22-11-130.ngrok.io/v1/portfolios/" + order.getPortfolioID())
                .retrieve()
                .bodyToMono(Portfolio.class)
                .block();

        return clientPortfolio;

    }

    public PortfolioStock getStockFromPortfolioByTickerWhenSelling(Order order){
        PortfolioStock clientStock = getClientPortfolioFromClientService(order).getPortfolioStockList()
                .stream()
                .filter(p -> p.getTicker().equalsIgnoreCase(order.getProduct().toString()))
                .findFirst()
                .get();

        return clientStock;
    }
}
