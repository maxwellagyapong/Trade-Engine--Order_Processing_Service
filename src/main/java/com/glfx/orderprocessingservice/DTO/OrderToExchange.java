package com.glfx.orderprocessingservice.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
public class OrderToExchange {
    private String product;
    private int quantity;
    private Double price;
    private String side;
    private String type;
}
