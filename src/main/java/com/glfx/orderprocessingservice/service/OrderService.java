package com.glfx.orderprocessingservice.service;

import com.glfx.orderprocessingservice.exceptions.InvalidActionException;
import com.glfx.orderprocessingservice.exceptions.InvalidOrderException;
import com.glfx.orderprocessingservice.exceptions.OrderNotFoundException;
import com.glfx.orderprocessingservice.messaging.MessagePublisher;
import com.glfx.orderprocessingservice.model.Order;
import com.glfx.orderprocessingservice.DTO.OrderToExchange;
import com.glfx.orderprocessingservice.repository.OrderRepository;
import com.glfx.orderprocessingservice.utils.Exchange;
import com.glfx.orderprocessingservice.utils.OrderType;
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

    @Autowired
    private SplitOrderService splitOrderService;

    @Autowired
    private MessagePublisher logMessage;


    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }


    public void createOrder(Order order) throws InvalidOrderException {

        if(orderValidator.validate(order)){
            if(order.getType().equalsIgnoreCase(OrderType.LIMIT.toString())){

                orderToExchange = new OrderToExchange(order.getProduct(), order.getQuantity(), order.getPrice(),
                        order.getSide(), order.getType());

                if(orderValidator.getExchanges().equalsIgnoreCase("exchange1")){

                    String response = webclient.build().post()
                            .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    order.setOrderIdFromExchange(response.substring(1,response.length()-1));
                    order.setStatus(Status.NOT_EXECUTED.toString());
                    order.setExchange(Exchange.exchange1.toString());
                    Order newOrder = orderRepository.save(order);
                    logMessage.makeLogMessage("New Order was made with the details: " + newOrder.toString());
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
                    order.setExchange(Exchange.exchange2.toString());
                    Order newOrder = orderRepository.save(order);
                    logMessage.makeLogMessage("New Order was made with the details: " + newOrder.toString());
                }

                else {

                    System.out.println("Splitting order will be done here!");
                    splitOrderService.splitOrderForLimitOrder(order);
                }
            }

            else {

                orderToExchange = new OrderToExchange(order.getProduct(), order.getQuantity(),
                        order.getSide(), order.getType());

                    if(orderValidator.getExchanges().equalsIgnoreCase("exchange1")){

                        String response = webclient.build().post()
                                .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                                .body(Mono.just(orderToExchange), OrderToExchange.class)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                        order.setStatus(Status.NOT_EXECUTED.toString());
                        order.setExchange(Exchange.exchange1.toString());
                        order.setOrderIdFromExchange(response.substring(1,response.length()-1));
                        Order newOrder = orderRepository.save(order);
                        logMessage.makeLogMessage("New Order was made with the details: " + newOrder.toString());
                    }

                    else if(orderValidator.getExchanges().equalsIgnoreCase("exchange2")){

                        String response = webclient.build().post()
                                .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                                .body(Mono.just(orderToExchange), OrderToExchange.class)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                        order.setStatus(Status.NOT_EXECUTED.toString());
                        order.setExchange(Exchange.exchange2.toString());
                        order.setOrderIdFromExchange(response.substring(1,response.length()-1));
                        Order newOrder = orderRepository.save(order);
                        logMessage.makeLogMessage("New Order was made with the details: " + newOrder.toString());
                    }

                    else {

                        System.out.println("Splitting order will be done here!");
                        splitOrderService.splitOrderForMarketOrder(order);

                    }
            }

        }

        else{
            throw new InvalidOrderException("Invalid Order");
        }

    }


    public Optional<Order> checkOrderStatus(Long id) throws OrderNotFoundException{

        Optional<Order> order = orderRepository.findById(id);
        //TODO: If auto-update order status is implemented, order status can be checked directly from DB without
        // hitting exchange's endpoint.
        if (order.isPresent()) {
            OrderToExchange response = webclient.build()
                .get()
                .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/"+order.get().getOrderIdFromExchange())
                .retrieve()
                .bodyToMono(OrderToExchange.class)
                .block();

            return order;
        }
        else{
            throw new OrderNotFoundException("Can't find Order for ID " + id);
        }
    }


    public void updateOrder(Long id, Order newOrder) throws OrderNotFoundException, InvalidActionException {

        Optional<Order> order = orderRepository.findById(id);

        if (order.isPresent()) {

            if(!order.get().getStatus().equalsIgnoreCase(Status.COMPLETED.toString())){
                orderToExchange.setQuantity(newOrder.getQuantity());
                orderToExchange.setPrice(newOrder.getPrice());

                if(order.get().getExchange().equalsIgnoreCase(Exchange.exchange1.toString())){
                    boolean response = Boolean.TRUE.equals(webclient.build()
                            .put()
                            .uri(exchange1 + "/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/" + order.get().getOrderIdFromExchange())
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block());
                }

                else if (order.get().getExchange().equalsIgnoreCase(Exchange.exchange2.toString())) {
                    boolean response = Boolean.TRUE.equals(webclient.build()
                            .put()
                            .uri(exchange2 + "/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/" + order.get().getOrderIdFromExchange())
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block());
                }

                else {
                    //TODO: Logic for updating multi-leg(split) orders
                }

                order.get().setQuantity(newOrder.getQuantity());
                order.get().setPrice(newOrder.getPrice());
                Order updatedOrder = orderRepository.save(order.get());
                logMessage.makeLogMessage("Order with ID: " + updatedOrder.getId() + " was made with the details: " + updatedOrder.toString());
            }
            else throw new InvalidActionException("Completed order cannot be modified!");

        }

        else{
            throw new OrderNotFoundException("Can't find Order for ID " + id);
        }

    }


    public void cancelOrder(Long id) throws OrderNotFoundException, InvalidActionException {
        Optional<Order> order = orderRepository.findById(id);

        if(order.isPresent()){
            if(!order.get().getStatus().equalsIgnoreCase(Status.COMPLETED.toString())){

                if(order.get().getExchange().equalsIgnoreCase(Exchange.exchange1.toString())){
                    boolean response = Boolean.TRUE.equals(webclient.build()
                            .delete()
                            .uri(exchange1 + "/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/" + order.get().getOrderIdFromExchange())
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block());
                }
                else if (order.get().getExchange().equalsIgnoreCase(Exchange.exchange2.toString())) {
                    boolean response = Boolean.TRUE.equals(webclient.build()
                            .delete()
                            .uri(exchange2 + "/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/" + order.get().getOrderIdFromExchange())
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block());
                }
                else {
                    //TODO: Logic for deleting multi-leg(split) orders
                }

                logMessage.makeLogMessage("Order with details: " + order.toString() + " was cancelled!");
                orderRepository.delete(order.get());
            }
            else throw new InvalidActionException("Completed order cannot be deleted!");
        }

        else throw new OrderNotFoundException("Can't find order with ID " + id);
    }
}
