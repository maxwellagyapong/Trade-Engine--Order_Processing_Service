package com.glfx.orderprocessingservice.model;

import com.glfx.orderprocessingservice.utils.OrderType;
import com.glfx.orderprocessingservice.utils.Product;
import com.glfx.orderprocessingservice.utils.Side;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    private Long portfolio;
    private Product product;
    private int quantity;
    @Column(name = "price",length=8,precision=2)
    private Double quotedPrice;
    private Side side;
    private OrderType type;
    @CreatedDate
    private LocalDateTime time;
    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Execution> executions;
    private String orderIdFromExchange;
    private String exchange;
    private String status;

}
