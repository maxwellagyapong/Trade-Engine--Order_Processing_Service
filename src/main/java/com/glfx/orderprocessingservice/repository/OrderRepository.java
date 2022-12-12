package com.glfx.orderprocessingservice.repository;

import com.glfx.orderprocessingservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

     //Set delete field to true in the DB rather than actually deleting it from the DB
    default void delete(Order order){
        order.setDeleted(true);
    }

    //Find all orders belonging to a particular Portfolio
    List<Order> findAllByPortfolioID(Long portfolioID);

    //Find all orders created on a particular date
    List<Order> findAllByDateCreated(Date date);

    //Find all orders based on whether completed, open or partially completed
    List<Order> findAllByStatus(String status);

}
