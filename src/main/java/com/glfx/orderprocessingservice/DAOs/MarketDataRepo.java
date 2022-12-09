package com.glfx.orderprocessingservice.DAOs;

import com.glfx.orderprocessingservice.DTO.MarketData;
import com.glfx.orderprocessingservice.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;


public class MarketDataRepo {

    @Autowired
    private WebClient.Builder webclient;

    public List<MarketData> marketDataFromExchange1(){
        Mono<List<MarketData>> res = webclient.build()
                .get()
                .uri("https://exchange.matraining.com/pd")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<MarketData>>() {
                });

        return res.block();
    }
    public List<MarketData> marketDataFromExchange2(){
        Mono<List<MarketData>> res = webclient.build()
                .get()
                .uri("https://exchange2.matraining.com/pd")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<MarketData>>() {
                });

        return res.block();
    }

    //Get market data on specific product from exchange 1
    public MarketData productMarketDataFromExchange1(Order order){

        return marketDataFromExchange1()
                .stream()
                .filter(md -> md.getTICKER().equalsIgnoreCase(order.getProduct().toString()))
                .findFirst()
                .get();
    }

    //Get market data on specific product from exchange 2
    public MarketData productMarketDataFromExchange2(Order order){

        return marketDataFromExchange2()
                .stream()
                .filter(md -> md.getTICKER().equalsIgnoreCase(order.getProduct().toString()))
                .findFirst()
                .get();
    }

}