package com.glfx.orderprocessingservice.DTO;

import java.util.List;

public class OrderBookExchanges {

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
