package com.glfx.orderprocessingservice.model;

import com.glfx.orderprocessingservice.utils.Product;
import com.glfx.orderprocessingservice.utils.Side;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Leg {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long clientId;
    private String idFromExchange;
    private Product product;
    private int quantity;
    private Double price;
    private Side side;
}