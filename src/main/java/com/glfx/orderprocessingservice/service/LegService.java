package com.glfx.orderprocessingservice.service;

import com.glfx.orderprocessingservice.DTO.OrderToExchange;
import com.glfx.orderprocessingservice.exceptions.InvalidActionException;
import com.glfx.orderprocessingservice.exceptions.OrderNotFoundException;
import com.glfx.orderprocessingservice.messaging.MessagePublisher;
import com.glfx.orderprocessingservice.model.Leg;
import com.glfx.orderprocessingservice.model.Order;
import com.glfx.orderprocessingservice.repository.LegRepository;
import com.glfx.orderprocessingservice.utils.Exchange;
import com.glfx.orderprocessingservice.utils.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class LegService {

    private String exchange1 = "https://exchange.matraining.com";

    private String exchange2 = "https://exchange2.matraining.com";

    @Autowired
    private WebClient.Builder webclient;

    @Autowired
    private LegRepository legRepository;

    @Autowired
    private OrderToExchange orderToExchange;

    @Autowired
    private MessagePublisher logMessage;

    public List<Leg> getAllLegs(){
        return legRepository.findAll();
    }

    public List<Leg> findLegsByOrderId(Long id){
        return legRepository.findByMainOrderId(id);
    }


    public void cancelLeg(Long id) throws OrderNotFoundException, InvalidActionException {
        Optional<Leg> leg = legRepository.findById(id);

        if(leg.isPresent()){

            if(!leg.get().getStatus().equalsIgnoreCase(Status.COMPLETED.toString())){

                if(leg.get().getExchange().equalsIgnoreCase(Exchange.exchange1.toString())){
                    boolean response = Boolean.TRUE.equals(webclient.build()
                            .delete()
                            .uri(exchange1 + "/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/" + leg.get().getOrderIdFromExchange())
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block());
                }
                else {
                    boolean response = Boolean.TRUE.equals(webclient.build()
                            .delete()
                            .uri(exchange2 + "/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/" + leg.get().getOrderIdFromExchange())
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block());
                }

                logMessage.makeLogMessage("Leg with details: " + leg.toString() + " was cancelled!");
                legRepository.delete(leg.get());
            }
            else throw new InvalidActionException("Completed leg cannot be deleted!");
        }

        else throw new OrderNotFoundException("Can't find leg with ID " + id);
    }

    public void modifyLeg(Long id, Order newOrder) throws OrderNotFoundException, InvalidActionException {

        Optional<Leg> leg = legRepository.findById(id);

        if (leg.isPresent()) {

            if(!leg.get().getStatus().equalsIgnoreCase(Status.COMPLETED.toString())){
                orderToExchange.setQuantity(newOrder.getQuantity());
                orderToExchange.setPrice(newOrder.getPrice());

                if(leg.get().getExchange().equalsIgnoreCase(Exchange.exchange1.toString())){
                    boolean response = Boolean.TRUE.equals(webclient.build()
                            .put()
                            .uri(exchange1 + "/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/" + leg.get().getOrderIdFromExchange())
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block());
                }

                else {
                    boolean response = Boolean.TRUE.equals(webclient.build()
                            .put()
                            .uri(exchange2 + "/15a204cd-9f59-45e9-8908-fbfe7f20480d/order/" + leg.get().getOrderIdFromExchange())
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block());
                }

                leg.get().setQuantity(newOrder.getQuantity());
//                leg.get().setPrice(newOrder.getPrice());
                Leg updatedOrder = legRepository.save(leg.get());
                logMessage.makeLogMessage("Order with ID: " + updatedOrder.getId() + " was made with the details: " + updatedOrder.toString());
            }
            else throw new InvalidActionException("Completed leg cannot be modified!");

        }

        else{
            throw new OrderNotFoundException("Can't find leg for ID " + id);
        }

    }
}
