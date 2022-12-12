package com.glfx.orderprocessingservice.DAOs;


import com.glfx.orderprocessingservice.DTO.OrderBook;
import com.glfx.orderprocessingservice.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class OrderBookRepository {

    @Autowired
    private WebClient.Builder webclient;

    public List<OrderBook> getProductOrderBook(Order order, String exchange){

        Mono<List<OrderBook>> res = webclient.build()
                .get()
                .uri(exchange+order.getProduct() +"/")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<OrderBook>>() {
                });

        return res.block();
    }

}
