package com.glfx.orderprocessingservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OrderBookWithExchange {

    private String product;
    private Double price;
    private int quantity;
    private String side;
    private String orderID;
    private String orderType;
    private int cumulatitiveQuantity;
    private double cumulatitivePrice;
    private List<Object> executions;
    private String exchange;

}
