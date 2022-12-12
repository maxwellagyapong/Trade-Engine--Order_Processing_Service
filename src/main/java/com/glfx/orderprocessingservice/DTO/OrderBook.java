package com.glfx.orderprocessingservice.DTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
public class OrderBook {

    private String product;
    private Double price;
    private int quantity;
    private String side;
    private String orderID;
    private String orderType;
    private int cumulatitiveQuantity;
    private double cumulatitivePrice;
    private List<Object> executions;
}
