package com.glfx.orderprocessingservice.exceptions;

public class NoOrdersFoundException extends Exception{
    public NoOrdersFoundException(String errorMessage){
        super(errorMessage);
    }
}
