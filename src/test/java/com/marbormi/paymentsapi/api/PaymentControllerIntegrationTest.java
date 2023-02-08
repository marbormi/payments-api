package com.marbormi.paymentsapi.api;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marbormi.paymentsapi.domain.CurrencyCode;
import com.marbormi.paymentsapi.domain.PaymentStatus;
import com.marbormi.paymentsapi.dtos.PaymentDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Payment Controller Integration Test")
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@Transactional
@Sql("PaymentControllerIntegrationTest.sql")
public class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String email = "payer@email.com";
    private final CurrencyCode currency = CurrencyCode.USD;
    private final BigDecimal amount = new BigDecimal("20.1");

    private final String basePaymentPath = "/payments/";

    @DisplayName("Get All Payments")
    @Test
    void getAllPayments() throws Exception {
        final String getAllPaymentsPath = basePaymentPath + "?page=1&size=2";
        final String responseDto = mockMvc.perform(
                        get(getAllPaymentsPath).accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final List<PaymentDTO> expectedPaymentDTOs = List.of(
                new PaymentDTO(
                        UUID.fromString("5c0cb601-e82e-4abf-91e9-811162c62e11"),
                        LocalDateTime.parse("2023-01-01T00:00:05"),
                        email,
                        PaymentStatus.PAID,
                        currency,
                        amount,
                        LocalDateTime.parse("2023-01-01T01:00:05")
                )
        );

        TypeReference<RestResponsePage<PaymentDTO>> typePaginated = new TypeReference<>() {};
        Page<PaymentDTO> paymentDTOS = objectMapper.readValue(responseDto, typePaginated);

        assertThat(paymentDTOS.getTotalPages()).isEqualTo(2);
        assertThat(paymentDTOS.getContent()).containsExactlyElementsOf(expectedPaymentDTOs);
    }
}
