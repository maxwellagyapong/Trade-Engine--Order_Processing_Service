package com.glfx.orderprocessingservice.model;

import com.glfx.orderprocessingservice.utils.OrderType;
import com.glfx.orderprocessingservice.utils.Product;
import com.glfx.orderprocessingservice.utils.Side;


public class OrderExchange {
    private Product product;
    private int quantity;
    private Double price;
    private Side side;
    private OrderType type;
}
