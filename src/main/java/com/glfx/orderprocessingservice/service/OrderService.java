package com.glfx.orderprocessingservice.service;

import com.glfx.orderprocessingservice.exceptions.InvalidOrderException;
import com.glfx.orderprocessingservice.exceptions.OrderNotFoundException;
import com.glfx.orderprocessingservice.model.Order;
import com.glfx.orderprocessingservice.model.OrderToExchange;
import com.glfx.orderprocessingservice.repository.OrderRepository;
import com.glfx.orderprocessingservice.utils.OrderType;
import com.glfx.orderprocessingservice.utils.Side;
import com.glfx.orderprocessingservice.utils.Status;
import com.glfx.orderprocessingservice.validators.OrderValidator;
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

    @Autowired
    private OrderValidator orderValidator;

    @Autowired
    private OrderToExchange orderToExchange;


    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }


    public void createOrder(Order order) throws InvalidOrderException {

        if(orderValidator.validate(order)){
            if(order.getType().equalsIgnoreCase(OrderType.LIMIT.toString())){
                orderToExchange.setProduct(order.getProduct());
                orderToExchange.setSide(order.getSide());
                orderToExchange.setPrice(order.getPrice());
                orderToExchange.setType(order.getType());
                orderToExchange.setQuantity(order.getQuantity());

                if(orderValidator.getExchanges().equalsIgnoreCase("exchange1")){

                    String response = webclient.build().post()
                            .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    order.setOrderIdFromExchange(response.substring(1,response.length()-1));
                    order.setStatus(Status.NOT_EXECUTED.toString());
                    orderRepository.save(order);
                }

                else if(orderValidator.getExchanges().equalsIgnoreCase("exchange2")){

                    String response = webclient.build().post()
                            .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    order.setStatus(Status.NOT_EXECUTED.toString());
                    order.setOrderIdFromExchange(response.substring(1,response.length()-1));
                    orderRepository.save(order);
                }

                else {

                    System.out.println("Splitting order will be done here!");
                    String response = webclient.build().post()
                            .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    order.setStatus(Status.NOT_EXECUTED.toString());
                    order.setOrderIdFromExchange(response.substring(1,response.length()-1));
                    orderRepository.save(order);
                }
            }

            else {
                    orderToExchange.setProduct(order.getProduct());
                    orderToExchange.setSide(order.getSide());
                    orderToExchange.setType(order.getType());
                    orderToExchange.setQuantity(order.getQuantity());

                    if(orderValidator.getExchanges().equalsIgnoreCase("exchange1")){

                        String response = webclient.build().post()
                                .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                                .body(Mono.just(orderToExchange), OrderToExchange.class)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                        order.setStatus(Status.NOT_EXECUTED.toString());
                        order.setOrderIdFromExchange(response.substring(1,response.length()-1));
                        orderRepository.save(order);
                    }

                    else if(orderValidator.getExchanges().equalsIgnoreCase("exchange2")){

                        String response = webclient.build().post()
                                .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                                .body(Mono.just(orderToExchange), OrderToExchange.class)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                        order.setStatus(Status.NOT_EXECUTED.toString());
                        order.setOrderIdFromExchange(response.substring(1,response.length()-1));
                        orderRepository.save(order);
                    }

                    else {

                        System.out.println("Splitting order will be done here!");
                        String response = webclient.build().post()
                                .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                                .body(Mono.just(orderToExchange), OrderToExchange.class)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                        order.setStatus(Status.NOT_EXECUTED.toString());
                        order.setOrderIdFromExchange(response.substring(1,response.length()-1));
                        orderRepository.save(order);
                    }
            }


        }

        else{
            throw new InvalidOrderException("Invalid Order");
        }

    }

    public Optional<Order> checkOrderStatus(Long id) throws OrderNotFoundException{

        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            OrderToExchange response = webclient.build()
                .get()
                .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/"+order.get().getOrderIdFromExchange())
                .retrieve()
                .bodyToMono(OrderToExchange.class)
                .block();
            return order;
        }
        else{
            throw new OrderNotFoundException("Can't find Order for ID " + id);
        }
    }


    public void updateOrder(Long id, Order order) throws OrderNotFoundException {
        Optional<Order> orderDB = orderRepository.findById(id);
//        boolean response = webclient.build()
//                .put()
//                .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/"+id)
//                .body(Mono.just(order), Order.class)
//                .retrieve()
//                .bodyToMono(Boolean.class)
//                .block();
        if (orderDB.isPresent()) {
            orderDB = Optional.of(order);
            orderRepository.save(orderDB.get());
        }
        else{
            throw new OrderNotFoundException("Can't find Order for ID " + id);
        }

    }

    public void cancelOrder(Long id) throws OrderNotFoundException {
        Optional<Order> order = orderRepository.findById(id);

        if(order.isPresent()){
                boolean response = webclient.build()
                        .delete()
                        .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/"+order.get().getOrderIdFromExchange())
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .block();
            }
        else throw new OrderNotFoundException("Can't find order with ID " + id);

        order.get().setDeleted(true);
        orderRepository.delete(order.get());
    }
}
