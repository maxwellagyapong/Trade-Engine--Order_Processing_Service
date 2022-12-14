package com.glfx.orderprocessingservice.controller;

import com.glfx.orderprocessingservice.exceptions.InvalidActionException;
import com.glfx.orderprocessingservice.exceptions.InvalidOrderException;
import com.glfx.orderprocessingservice.exceptions.OrderNotFoundException;
import com.glfx.orderprocessingservice.model.Leg;
import com.glfx.orderprocessingservice.model.Order;
import com.glfx.orderprocessingservice.service.ArbFinder;
import com.glfx.orderprocessingservice.service.LegService;
import com.glfx.orderprocessingservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private LegService legService;

    @Autowired
    private ArbFinder arbFinder;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getAllOrders(){
        return orderService.getAllOrders();
    }

    @GetMapping("/{orderId}/status")
    @ResponseStatus(HttpStatus.OK)
    public String getOrderStatus(@PathVariable("orderId") Long id) throws OrderNotFoundException {
        return orderService.checkOrderStatus(id);
    }

    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable("orderId") Long id) throws OrderNotFoundException{
        return orderService.getOrder(id);
    }

    @GetMapping("/legs")
    @ResponseStatus(HttpStatus.OK)
    public List<Leg> getAllLegs(){
        return legService.getAllLegs();
    }

    @GetMapping("/{orderId}/legs")
    @ResponseStatus(HttpStatus.OK)
    public List<Leg> getAllLegsOfAnOrder(@PathVariable Long orderId){
        return legService.findLegsByOrderId(orderId);
    }



    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createOrder(@Valid @RequestBody Order order) throws InvalidOrderException {
        orderService.createOrder(order);
    }

//    @PutMapping("/{orderId}")
//    @ResponseStatus(HttpStatus.OK)
//    public void updateOrder(@Validated @RequestBody Order order, @PathVariable("orderId") Long id) throws OrderNotFoundException, InvalidActionException {
//        orderService.updateOrder(id, order);
//    }

//    @DeleteMapping("/{orderId}")
//    @ResponseStatus(HttpStatus.OK)
//    public void cancelOrder(@PathVariable("orderId") Long id) throws OrderNotFoundException, InvalidActionException {
//        orderService.cancelOrder(id);
//    }

    @DeleteMapping("/{orderId}/{legId}")
    @ResponseStatus(HttpStatus.OK)
    public void cancelLeg(@PathVariable Long orderId, @PathVariable Long legId) throws OrderNotFoundException, InvalidActionException {
        legService.cancelLeg(legId);
    }

    @PutMapping("/{orderId}/{legId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateOrder(@RequestBody Order order, @PathVariable Long orderId, @PathVariable Long legId) throws OrderNotFoundException, InvalidActionException {
        legService.modifyLeg(legId, order);
    }
    //Trying to automate Trading
    @PostMapping("/automate")
    @ResponseStatus(HttpStatus.OK)
    public void automateTrade(){
        arbFinder.getArb();
    }
}

