package com.glfx.orderprocessingservice.exceptions;

public class InvalidPortfolioException extends Exception{
    public InvalidPortfolioException(String errorMessage){
        super(errorMessage);
    }
}
