package com.marbormi.paymentsapi.mappers;

import com.marbormi.paymentsapi.domain.Payment;
import com.marbormi.paymentsapi.dtos.PaymentCreationDTO;
import com.marbormi.paymentsapi.dtos.PaymentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDTO toPaymentDto(Payment payment);
    Payment toPayment(PaymentCreationDTO creationDTO);
}
