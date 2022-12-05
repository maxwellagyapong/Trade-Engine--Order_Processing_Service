package com.glfx.orderprocessingservice.service;

import com.glfx.orderprocessingservice.exceptions.InvalidOrderException;
import com.glfx.orderprocessingservice.exceptions.OrderNotFoundException;
import com.glfx.orderprocessingservice.model.Order;
import com.glfx.orderprocessingservice.repository.OrderRepository;
import com.glfx.orderprocessingservice.utils.OrderType;
import com.glfx.orderprocessingservice.utils.Side;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private String exchange1 = "https://exchange.matraining.com";

    private String exchange2 = "https://exchange2.matraining.com";

    @Autowired
    private WebClient.Builder webclient;

    @Autowired
    private OrderRepository orderRepository;

    public List<Order> getAllPortfolioOrders(Long portfolioID){
        //List<Order> orders =  orderRepository.findAll();
        return orderRepository.findByPortfolioID(portfolioID);
    }

    public Optional<Order> checkOrderStatus(Long id) throws OrderNotFoundException{
        Order response = webclient.build()
                .get()
                .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/"+id)
                .retrieve()
                .bodyToMono(Order.class)
                .block();
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            return order;
        }
        else{
            throw new OrderNotFoundException("Can't find Order for ID " + id);
        }
    }

    public void marketOrder(Order order){
        if(order.getSide() == Side.Buy){
            orderRepository.save(order);
        }
        else if (order.getSide() == Side.Sell) {
            orderRepository.save(order);
        }
    }

    public void limitOrder(Order order) throws InvalidOrderException{
        if(order.getQuotedPrice() <= 0) {
            throw new InvalidOrderException("Quoted price must be greater than 0 for limit orders!");
        }
        else {
            if (order.getSide() == Side.Buy) {
                orderRepository.save(order);
            }
            else if (order.getSide() == Side.Sell) {
                orderRepository.save(order);
            }
        }
    }

    public void sellOrder(Order order) throws InvalidOrderException {
        if(order.getType() == OrderType.Limit_Order){
            limitOrder(order);
        }
        else if (order.getType() == OrderType.Market_Order) {
            marketOrder(order);
        }
    }

    public void buyOrder(Order order) throws InvalidOrderException {
        if(order.getType() == OrderType.Limit_Order){
            limitOrder(order);
        }
        else if (order.getType() == OrderType.Market_Order){
            marketOrder(order);
        }
    }

    public void createOrder(Order order) throws InvalidOrderException {
        if(order.getQuantity() <= 0){
            throw new InvalidOrderException("Stock quantity must be greater than zero to place an order!");
        }
        else {
            if (order.getSide() == Side.Buy) {
                buyOrder(order);
            } else if (order.getSide() == Side.Sell) {
                sellOrder(order);
            }
            String response = webclient.build().post()
                    .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                    .body(Mono.just(order), Order.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            //order.setId(response.substring(1,response.length()-1));
            orderRepository.save(order);
        }
    }

    public void updateOrder(Long id, Order order) throws OrderNotFoundException {
        Optional<Order> orderDB = orderRepository.findById(id);
        boolean response = webclient.build()
                .put()
                .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/"+id)
                .body(Mono.just(order), Order.class)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        if (orderDB.isPresent()) {
            orderDB = Optional.of(order);
            orderRepository.save(orderDB.get());
        }
        else{
            throw new OrderNotFoundException("Can't find Order for ID " + id);
        }

    }

    public void cancelOrder(Long id){
        boolean response = webclient.build()
                .delete()
                .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/"+id)
                .retrieve()
                .bodyToMono(Boolean.class)
                        .block();
        orderRepository.deleteById(id);
    }
}
