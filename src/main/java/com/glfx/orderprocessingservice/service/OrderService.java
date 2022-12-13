package com.glfx.orderprocessingservice.service;

import com.glfx.orderprocessingservice.exceptions.InvalidActionException;
import com.glfx.orderprocessingservice.exceptions.InvalidOrderException;
import com.glfx.orderprocessingservice.exceptions.OrderNotFoundException;
import com.glfx.orderprocessingservice.messaging.MessagePublisher;
import com.glfx.orderprocessingservice.model.Leg;
import com.glfx.orderprocessingservice.model.Order;
import com.glfx.orderprocessingservice.DTO.OrderToExchange;
import com.glfx.orderprocessingservice.repository.LegRepository;
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

    @Autowired
    private LegRepository legRepository;

    @Autowired
    private Leg leg;

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
//                    order.setOrderIdFromExchange(response.substring(1,response.length()-1));
//                    order.setExchange(Exchange.exchange1.toString());
                    order.setStatus(Status.NOT_EXECUTED.toString());
                    Order newOrder = orderRepository.save(order);

                    leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                            orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);

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
//                    order.setOrderIdFromExchange(response.substring(1,response.length()-1));
//                    order.setExchange(Exchange.exchange2.toString());
                    Order newOrder = orderRepository.save(order);

                    leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                            orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);

                    logMessage.makeLogMessage("New Order was made with the details: " + newOrder.toString());
                }

                else {

                    System.out.println("Splitting order will be done here!");
//                    splitOrderService.splitOrderForLimitOrder(order);
                    String response = webclient.build().post()
                            .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    order.setStatus(Status.NOT_EXECUTED.toString());
//                    order.setExchange(Exchange.exchange2.toString());
//                    order.setOrderIdFromExchange(response.substring(1,response.length()-1));
                    Order newOrder = orderRepository.save(order);

                    leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange2.toString(), order.getProduct(),
                            orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);

                    logMessage.makeLogMessage("New Order was made with the details: " + newOrder.toString());
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
//                    order.setExchange(Exchange.exchange1.toString());
//                    order.setOrderIdFromExchange(response.substring(1,response.length()-1));
                    Order newOrder = orderRepository.save(order);

                    leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                            orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);

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
//                    order.setExchange(Exchange.exchange2.toString());
//                    order.setOrderIdFromExchange(response.substring(1,response.length()-1));
                    Order newOrder = orderRepository.save(order);

                    leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                            orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);

                    logMessage.makeLogMessage("New Order was made with the details: " + newOrder.toString());
                }

                else {

                    System.out.println("Splitting order will be done here!");
                    //splitOrderService.splitOrderForMarketOrder(order);

                    String response = webclient.build().post()
                            .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    order.setStatus(Status.NOT_EXECUTED.toString());
//                    order.setExchange(Exchange.exchange2.toString());
//                    order.setOrderIdFromExchange(response.substring(1,response.length()-1));
                    Order newOrder = orderRepository.save(order);

                    leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange2.toString(), order.getProduct(),
                            orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);


                    logMessage.makeLogMessage("New Order was made with the details: " + newOrder.toString());

                }
            }

        }

        else{
            throw new InvalidOrderException("Invalid Order");
        }

    }


    public Order getOrder(Long id) throws OrderNotFoundException {
        Optional<Order> order = orderRepository.findById(id);
        if(order.isPresent()){
            return order.get();
        }
        else {
            throw new OrderNotFoundException("Can't find order with ID "+id);
        }
    }

    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }


    public String checkOrderStatus(Long id) throws OrderNotFoundException{

        Optional<Order> order = orderRepository.findById(id);
        if(order.isPresent()){
            if(order.get().getStatus().equals(Status.COMPLETED.toString())){
                return "COMPLETED!";
            }
            else if (order.get().getStatus().equals(Status.PARTIALLY_EXECUTED.toString())) {
                return "PARTIALLY COMPLETED!";
            }
            else return "NOT EXECUTED!";
        }
        else{
            throw new OrderNotFoundException("Can't find Order for ID " + id);
        }
    }


    //Moved update functionality to Leg instead of Order
//    public void updateOrder(Long id, Order newOrder) throws OrderNotFoundException, InvalidActionException {
//
//        Optional<Order> order = orderRepository.findById(id);
//
//        if (order.isPresent()) {
//
//            if(!order.get().getStatus().equalsIgnoreCase(Status.COMPLETED.toString())){
//                orderToExchange.setQuantity(newOrder.getQuantity());
//                orderToExchange.setPrice(newOrder.getPrice());
//
//                if(order.get().getExchange().equalsIgnoreCase(Exchange.exchange1.toString())){
//                    boolean response = Boolean.TRUE.equals(webclient.build()
//                            .put()
//                            .uri(exchange1 + "/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/" + order.get().getOrderIdFromExchange())
//                            .body(Mono.just(orderToExchange), OrderToExchange.class)
//                            .retrieve()
//                            .bodyToMono(Boolean.class)
//                            .block());
//                }
//
//                else if (order.get().getExchange().equalsIgnoreCase(Exchange.exchange2.toString())) {
//                    boolean response = Boolean.TRUE.equals(webclient.build()
//                            .put()
//                            .uri(exchange2 + "/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/" + order.get().getOrderIdFromExchange())
//                            .body(Mono.just(orderToExchange), OrderToExchange.class)
//                            .retrieve()
//                            .bodyToMono(Boolean.class)
//                            .block());
//                }
//
//                else {
//                    //TODO: Logic for updating multi-leg(split) orders
//                    throw new InvalidActionException("This kind of orders cannot be updated!"); //Use this for now
//                }
//
//                order.get().setQuantity(newOrder.getQuantity());
//                order.get().setPrice(newOrder.getPrice());
//                Order updatedOrder = orderRepository.save(order.get());
//                logMessage.makeLogMessage("Order with ID: " + updatedOrder.getId() + " was made with the details: " + updatedOrder.toString());
//            }
//            else throw new InvalidActionException("Completed order cannot be modified!");
//
//        }
//
//        else{
//            throw new OrderNotFoundException("Can't find Order for ID " + id);
//        }
//
//    }


//    Moved cancelling functionality to Leg instead of Order
//    public void cancelOrder(Long id) throws OrderNotFoundException, InvalidActionException {
//        Optional<Order> order = orderRepository.findById(id);
//
//        if(order.isPresent()){
//            if(!order.get().getStatus().equalsIgnoreCase(Status.COMPLETED.toString())){
//
//                if(order.get().getExchange().equalsIgnoreCase(Exchange.exchange1.toString())){
//                    boolean response = Boolean.TRUE.equals(webclient.build()
//                            .delete()
//                            .uri(exchange1 + "/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/" + order.get().getOrderIdFromExchange())
//                            .retrieve()
//                            .bodyToMono(Boolean.class)
//                            .block());
//                }
//                else if (order.get().getExchange().equalsIgnoreCase(Exchange.exchange2.toString())) {
//                    boolean response = Boolean.TRUE.equals(webclient.build()
//                            .delete()
//                            .uri(exchange2 + "/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/" + order.get().getOrderIdFromExchange())
//                            .retrieve()
//                            .bodyToMono(Boolean.class)
//                            .block());
//                }
//                else {
//                    //TODO: Logic for deleting multi-leg(split) orders
//                    throw new InvalidActionException("This kind of orders cannot be canceled!"); //Use this for now
//                }
//
//                logMessage.makeLogMessage("Order with details: " + order.toString() + " was cancelled!");
//                orderRepository.delete(order.get());
//            }
//            else throw new InvalidActionException("Completed order cannot be deleted!");
//        }
//
//        else throw new OrderNotFoundException("Can't find order with ID " + id);
//    }

}
