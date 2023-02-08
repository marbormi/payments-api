package com.marbormi.paymentsapi.exceptions;

import java.util.UUID;

public class PaymentAlreadyPaid extends RuntimeException{
    public PaymentAlreadyPaid(UUID id) {
        super(String.format("Payment %s already paid", id));
    }
}
