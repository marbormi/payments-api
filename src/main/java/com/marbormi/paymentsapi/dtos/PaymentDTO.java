package com.marbormi.paymentsapi.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.marbormi.paymentsapi.domain.CurrencyCode;
import com.marbormi.paymentsapi.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentDTO(
        @JsonProperty UUID id,
        @JsonProperty LocalDateTime createdDate,
        @JsonProperty String payerEmail,
        @JsonProperty PaymentStatus status,
        @JsonProperty CurrencyCode currency,
        @JsonProperty BigDecimal amount,
        @JsonProperty LocalDateTime paidDate
) {
}
