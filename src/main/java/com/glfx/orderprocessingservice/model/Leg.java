package com.glfx.orderprocessingservice.model;

import jakarta.persistence.*;
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
    private String product;
    private int quantity;
    private String side;
    private String status;
    private Long mainOrderId;
    @Column(name="is_deleted")
    private boolean isDeleted = false;

    public Leg(String orderIdFromExchange, String exchange, String product, int quantity, String side, String status, Long mainOrderId) {
        this.orderIdFromExchange = orderIdFromExchange;
        this.exchange = exchange;
        this.product = product;
        this.quantity = quantity;
        this.side = side;
        this.status = status;
        this.mainOrderId = mainOrderId;
    }
}