package com.marbormi.paymentsapi.services;

import com.marbormi.paymentsapi.domain.Payment;
import com.marbormi.paymentsapi.exceptions.PaymentNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PaymentService {
    Page<Payment> findAll(Pageable pageable);
    Payment findById(UUID id) throws PaymentNotFound;
    Payment save(Payment payment);
    boolean deleteIfUnpaid(UUID id) throws PaymentNotFound;
    Payment markAsPaid(UUID id) throws PaymentNotFound;
}
