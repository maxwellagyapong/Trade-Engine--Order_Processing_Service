package com.glfx.orderprocessingservice.exceptions;

public class OrderNotFoundException extends Exception{
    public OrderNotFoundException(String errorMessage){
        super(errorMessage);
    }
}
