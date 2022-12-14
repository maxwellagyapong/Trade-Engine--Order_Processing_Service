package com.glfx.orderprocessingservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderToExchange {
    private String product;
    private int quantity;
    private Double price;
    private String side;
    private String type;

    public OrderToExchange(String product, int quantity, String side, String type) {
        this.product = product;
        this.quantity = quantity;
        this.side = side;
        this.type = type;
    }


}
