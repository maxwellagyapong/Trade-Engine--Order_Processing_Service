package com.glfx.orderprocessingservice.exceptions;

public class InvalidOrderException extends Exception{
    public InvalidOrderException(String errorMessage) {
        super(errorMessage);
    }
}
