package com.glfx.orderprocessingservice.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotNull(message = "Portfolio id cannot be null!")
    private Long portfolioID;
    @NotNull(message = "You must select a product!")
    private String product;
    private int quantity;
    @Column(precision=2)
    private Double price;
    private String side;
    @NotNull(message = "Order must have a type. Either LIMIT or MARKET!")
    private String type;
    @Column(name = "date_created", nullable = false)
    private Date dateCreated;
    @Column(name = "date_modified")
    private Date dateModified;
//    private String orderIdFromExchange;
    private String status;
    @Column(name="is_deleted")
    private boolean isDeleted = false;
//    private String exchange;
    @OneToMany(mappedBy = "mainOrderId")
    private List<Leg> legs;



    @PrePersist
    protected void prePersist() {
        if (this.dateCreated == null) dateCreated = new Date();
        if (this.dateModified == null) dateModified = new Date();
    }

    @PreUpdate
    protected void preUpdate() {
        this.dateModified= new Date();
    }

//    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    private List<Execution> executions;


}
