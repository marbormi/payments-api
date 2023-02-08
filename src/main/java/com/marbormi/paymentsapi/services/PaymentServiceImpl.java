package com.marbormi.paymentsapi.services;

import com.marbormi.paymentsapi.domain.Payment;
import com.marbormi.paymentsapi.domain.PaymentStatus;
import com.marbormi.paymentsapi.exceptions.PaymentNotFound;
import com.marbormi.paymentsapi.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService{

    private final PaymentRepository repository;

    @Override
    public Page<Payment> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Payment findById(UUID id) throws PaymentNotFound {
        return repository.findById(id).orElseThrow(() -> new PaymentNotFound(id));
    }

    @Override
    public Payment save(Payment payment) {
        return repository.save(payment);
    }

    @Override
    public boolean deleteIfUnpaid(UUID id) throws PaymentNotFound {
        Payment payment = findById(id);
        if(PaymentStatus.UNPAID == payment.getStatus()) {
            repository.delete(payment);
            return true;
        }
        return false;
    }

    @Override
    public Payment markAsPaid(UUID id) throws PaymentNotFound{
        Payment payment = findById(id);
        if(PaymentStatus.UNPAID == payment.getStatus()) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidDate(LocalDateTime.now());
            payment = save(payment);
        }

        return payment;
    }
}
