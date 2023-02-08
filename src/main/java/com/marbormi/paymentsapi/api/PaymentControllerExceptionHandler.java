package com.marbormi.paymentsapi.api;

import com.marbormi.paymentsapi.exceptions.PaymentAlreadyPaid;
import com.marbormi.paymentsapi.exceptions.PaymentNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class PaymentControllerExceptionHandler {

    @ExceptionHandler(value = {PaymentNotFound.class})
    public ResponseEntity<Object> handlePaymentNotFound(PaymentNotFound exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {PaymentAlreadyPaid.class})
    public ResponseEntity<Object> handlePaymentAlreadyPaid(PaymentAlreadyPaid exception){
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.FORBIDDEN);
    }
}
