package com.glfx.orderprocessingservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.glfx.orderprocessingservice.utils.OrderType;
import com.glfx.orderprocessingservice.utils.Product;
import com.glfx.orderprocessingservice.utils.Side;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;


@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    private Long portfolioID;
    private Side side;
    private Product product;
    private double quotedPrice;
    private int quantity;
    private OrderType type;
    @CreatedDate
    private Instant createdAt;
}
