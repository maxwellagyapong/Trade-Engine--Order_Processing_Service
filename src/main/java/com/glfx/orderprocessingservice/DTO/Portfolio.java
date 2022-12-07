package com.glfx.orderprocessingservice.portfolio.portfolioDTO;

import lombok.Getter;

@Getter
public class Portfolio {
    private Long id;
    private String name;
    private double amount;
    private Long clientId;
}
