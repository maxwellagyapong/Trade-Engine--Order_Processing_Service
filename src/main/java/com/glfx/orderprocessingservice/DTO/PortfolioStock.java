package com.glfx.orderprocessingservice.DTO;

import lombok.Getter;

@Getter
public class PortfolioStock {
    private Long id;
    private String ticker;
    private Long portfolioId;
    private int quantity;
    private boolean isDeleted;
}
