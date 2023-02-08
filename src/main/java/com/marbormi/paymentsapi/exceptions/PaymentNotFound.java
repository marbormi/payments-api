package com.marbormi.paymentsapi.exceptions;

import java.util.UUID;

public class PaymentNotFound extends RuntimeException{

    public PaymentNotFound(UUID id) {
        super(String.format("Payment %s not found", id));
    }
}
