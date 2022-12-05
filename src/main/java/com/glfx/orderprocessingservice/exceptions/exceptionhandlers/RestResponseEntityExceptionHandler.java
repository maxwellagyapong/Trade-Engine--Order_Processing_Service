package com.glfx.orderprocessingservice.exceptions.exceptionhandlers;

import com.glfx.orderprocessingservice.exceptions.InvalidOrderException;
import com.glfx.orderprocessingservice.exceptions.NoOrdersFoundException;
import com.glfx.orderprocessingservice.exceptions.OrderNotFoundException;
import com.glfx.orderprocessingservice.model.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@ResponseStatus
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorMessage> orderNotFoundException(OrderNotFoundException exception, WebRequest request){
        ErrorMessage message = new ErrorMessage(HttpStatus.NOT_FOUND, exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    @ExceptionHandler(NoOrdersFoundException.class)
    public ResponseEntity<ErrorMessage> noOrdersFoundException(NoOrdersFoundException exception){
        ErrorMessage message = new ErrorMessage(HttpStatus.NOT_FOUND, exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    @ExceptionHandler(InvalidOrderException.class) // TODO: rewrite this as a generic db exception handler
    public ResponseEntity<ErrorMessage> invalidOrderException(InvalidOrderException exception){
        ErrorMessage message = new ErrorMessage(HttpStatus.BAD_REQUEST, exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }


}
