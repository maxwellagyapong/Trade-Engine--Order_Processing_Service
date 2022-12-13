package com.glfx.orderprocessingservice.repository;

import com.glfx.orderprocessingservice.model.Leg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LegRepository extends JpaRepository<Leg, Long> {

    public List<Leg> findByMainOrderId(Long mainOrderId);

    //Overriding the default delete query in JpaRepository
    //Set delete field to true in the DB rather than actually deleting it from the DB
    default void delete(Leg leg){
        leg.setDeleted(true);
    }
}
