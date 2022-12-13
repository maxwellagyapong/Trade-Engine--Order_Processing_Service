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
                .uri("https://d9bb-102-22-11-130.eu.ngrok.io/v1/portfolios/" + order.getPortfolioID())
                .retrieve()
                .bodyToMono(Portfolio.class)
                .block();

        return clientPortfolio;

    }

    public PortfolioStock getStockFromPortfolioByTickerWhenSelling(Order order){
        PortfolioStock clientStock = getClientPortfolioFromClientService(order).getPortfolioStockList()
                .stream()
                .filter(p -> p.getTicker().equalsIgnoreCase(order.getProduct()))
                .findFirst()
                .get();

        return clientStock;
    }
}
