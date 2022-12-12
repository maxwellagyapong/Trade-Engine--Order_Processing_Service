package com.glfx.orderprocessingservice.controller;

import com.glfx.orderprocessingservice.exceptions.InvalidActionException;
import com.glfx.orderprocessingservice.exceptions.InvalidOrderException;
import com.glfx.orderprocessingservice.exceptions.OrderNotFoundException;
import com.glfx.orderprocessingservice.model.Order;
import com.glfx.orderprocessingservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getAllOrders(){
        return orderService.getAllOrders();
    }

    @GetMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Order> getOrder(@PathVariable("orderId") Long id) throws OrderNotFoundException {
        return orderService.checkOrderStatus(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createOrder(@Valid @RequestBody Order order) throws InvalidOrderException {
        orderService.createOrder(order);
    }

    @PutMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateOrder(@Validated @RequestBody Order order, @PathVariable("orderId") Long id) throws OrderNotFoundException, InvalidActionException {
        orderService.updateOrder(id, order);
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public void cancelOrder(@PathVariable("orderId") Long id) throws OrderNotFoundException, InvalidActionException {
        orderService.cancelOrder(id);
    }


}

