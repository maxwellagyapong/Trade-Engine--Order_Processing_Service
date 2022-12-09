package com.glfx.orderprocessingservice.DTO;

import lombok.Getter;

import java.util.List;

@Getter
public class Portfolio {
    private Long id;
    private String name;
    private double amount;
    private Long clientId;
    private List<PortfolioStock> portfolioStockList;
}
