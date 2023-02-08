package com.marbormi.paymentsapi.dtos;

import com.marbormi.paymentsapi.domain.CurrencyCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentCreationDTO(
        @NotBlank @Email String payerEmail,
        @NotNull CurrencyCode currency,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount
) {
}
