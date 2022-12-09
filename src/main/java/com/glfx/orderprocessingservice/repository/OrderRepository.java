package com.glfx.orderprocessingservice.repository;

import com.glfx.orderprocessingservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

//    List<Order> findByPortfolioID(Long portfolioID);

    //Set delete field to true in the DB rather than actually deleting it from the DB
     default void delete(Order order){
        order.setDeleted(true);
    };

}
