package com.marbormi.paymentsapi.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marbormi.paymentsapi.domain.CurrencyCode;
import com.marbormi.paymentsapi.domain.PaymentStatus;
import com.marbormi.paymentsapi.dtos.PaymentCreationDTO;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private final UUID unpaidPaymentId = UUID.fromString("c5003832-7c62-4745-ac01-82bcf69215db");
    private final UUID paidPaymentId = UUID.fromString("e8fca603-9361-40a9-a1db-ac0b4b2d2b02");
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

    @DisplayName("Get Existent Payment")
    @Test
    void getPayment() throws Exception {
        final String existentPaymentPath = String.format("/payments/%s", unpaidPaymentId);
        final String responseDto = mockMvc.perform(
                        get(existentPaymentPath).accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final PaymentDTO expectedPaymentDto = new PaymentDTO(
                unpaidPaymentId,
                LocalDateTime.parse("2023-01-30T11:00:05"),
                email,
                PaymentStatus.UNPAID,
                currency,
                amount,
                null
        );

        final PaymentDTO paymentDto = objectMapper.readValue(responseDto, PaymentDTO.class);

        assertThat(paymentDto).isEqualTo(expectedPaymentDto);
    }

    @DisplayName("Get Non-Existent Payment")
    @Test
    void getNonExistentPayment() throws Exception {
        final String nonExistentPaymentPath = "/payments/f65507f5-1182-4e68-b448-3c7f3c32f1f6";
        mockMvc.perform(
                        get(nonExistentPaymentPath).accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @DisplayName("Create Payment")
    @Test
    void createPayment() throws Exception {
        final String responseDto = mockMvc.perform(
                        post(basePaymentPath)
                                .content(getCreateDto())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)

                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final LocalDateTime requestedAt = LocalDateTime.now();

        final PaymentDTO paymentDto = objectMapper.readValue(responseDto, PaymentDTO.class);
        assertThat(paymentDto).isNotNull();
        assertThat(paymentDto.id()).isNotNull();
        assertThat(paymentDto.createdDate()).isNotNull()
                .isCloseTo(requestedAt,within(1, ChronoUnit.SECONDS));
        assertThat(paymentDto.payerEmail()).isEqualTo(email);
        assertThat(paymentDto.status()).isEqualTo(PaymentStatus.UNPAID);
        assertThat(paymentDto.currency()).isEqualTo(currency);
        assertThat(paymentDto.amount()).isEqualTo(amount);
        assertThat(paymentDto.paidDate()).isNull();
    }

    public String getCreateDto() throws JsonProcessingException {
        return objectMapper.writeValueAsString(
                new PaymentCreationDTO(
                        email,
                        currency,
                        amount
                )
        );
    }

    @DisplayName("Create Payment with empty/null payer email")
    @Test
    void createPaymentWithInvalidEmail() throws Exception {
        createPaymentWithInvalidContent("{\"payerEmail\": null, \"currency\": \"USD\", \"amount\": 10.55}");
        createPaymentWithInvalidContent("{\"payerEmail\": \"\", \"currency\": \"USD\", \"amount\": 10.55}");
        createPaymentWithInvalidContent("{\"payerEmail\": \"invalid_email\", \"currency\": \"USD\", \"amount\": 10.55}");
        createPaymentWithInvalidContent("{\"currency\": \"USD\", \"amount\": 10.55}");
    }

    @DisplayName("Create Payment with empty/null currency")
    @Test
    void createPaymentWithEmptyOrNullCurrency() throws Exception {
        createPaymentWithInvalidContent("{\"payerEmail\": \"payer@email.com\", \"currency\": null, \"amount\": 10.55}");
        createPaymentWithInvalidContent("{\"payerEmail\": \"payer@email.com\", \"currency\": \"\", \"amount\": 10.55}");
        createPaymentWithInvalidContent("{\"payerEmail\": \"payer@email.com\", \"amount\": 10.55}");
    }

    @DisplayName("Create Payment with invalid amount")
    @Test
    void createPaymentWithInvalidAmount() throws Exception {
        createPaymentWithInvalidContent("{\"payerEmail\": \"payer@email.com\", \"currency\": \"USD\", \"amount\": 0}");
        createPaymentWithInvalidContent("{\"payerEmail\": \"payer@email.com\", \"currency\": \"USD\"}");
        createPaymentWithInvalidContent("{\"payerEmail\": \"payer@email.com\", \"currency\": \"USD\", \"amount\": -1.0}");
    }

    private void createPaymentWithInvalidContent(String content) throws Exception {
        mockMvc.perform(
                        post(basePaymentPath)
                                .content(content)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)

                )
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Mark Payment as paid")
    @Test
    void markPaymentAsPaid() throws Exception {
        final String updatePaymentPath = String.format("/payments/%s/paid", unpaidPaymentId);
        final String responseDto = mockMvc.perform(
                        patch(updatePaymentPath)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final LocalDateTime requestedAt = LocalDateTime.now();

        final PaymentDTO paymentDto = objectMapper.readValue(responseDto, PaymentDTO.class);
        assertThat(paymentDto.status()).isEqualTo(PaymentStatus.PAID);
        assertThat(paymentDto.paidDate()).isNotNull()
                .isCloseTo(requestedAt, within(1, ChronoUnit.SECONDS));
    }

    @DisplayName("Mark Payment already paid, as paid")
    @Test
    void markPaymentAsPaidAgain() throws Exception {
        final String updatePaymentPath = String.format("/payments/%s/paid", paidPaymentId);
        final String responseDto = mockMvc.perform(
                        patch(updatePaymentPath)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final LocalDateTime paidAt = LocalDateTime.parse("2023-01-01T01:00:05");

        final PaymentDTO paymentDto = objectMapper.readValue(responseDto, PaymentDTO.class);
        assertThat(paymentDto.status()).isEqualTo(PaymentStatus.PAID);
        assertThat(paymentDto.paidDate()).isNotNull()
                .isEqualTo(paidAt);
    }

    @DisplayName("Delete unpaid payment")
    @Test
    void deletePaymentIfUnpaid() throws Exception {
        final String deletePaymentPath = String.format("/payments/%s", unpaidPaymentId);
        mockMvc.perform(
                        delete(deletePaymentPath)
                )
                .andExpect(status().isNoContent());
    }

    @DisplayName("Try to delete paid payment")
    @Test
    void deletePaymentPaid() throws Exception {
        final String deletePaymentPath = String.format("/payments/%s", paidPaymentId);
        mockMvc.perform(
                        delete(deletePaymentPath)
                )
                .andExpect(status().isForbidden());
    }
}
