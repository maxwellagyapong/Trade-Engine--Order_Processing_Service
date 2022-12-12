package com.glfx.orderprocessingservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.stereotype.Component;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Component
public class Leg {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderIdFromExchange;
    private String exchange;
    private Double price;
    private String side;
    @NotNull(message = "Order must have a type. Either LIMIT or MARKET!")
    private String type;
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order mainOrder;
}