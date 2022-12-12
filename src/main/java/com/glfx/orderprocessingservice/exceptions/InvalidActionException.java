package com.glfx.orderprocessingservice.exceptions;

public class InvalidActionException extends Exception{
    public InvalidActionException(String errorMessage){
        super(errorMessage);
    }
}
