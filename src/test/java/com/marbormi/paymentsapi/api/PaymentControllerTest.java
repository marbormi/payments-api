package com.marbormi.paymentsapi.api;

import com.marbormi.paymentsapi.domain.CurrencyCode;
import com.marbormi.paymentsapi.domain.Payment;
import com.marbormi.paymentsapi.domain.PaymentStatus;
import com.marbormi.paymentsapi.dtos.PaymentCreationDTO;
import com.marbormi.paymentsapi.dtos.PaymentDTO;
import com.marbormi.paymentsapi.exceptions.PaymentAlreadyPaid;
import com.marbormi.paymentsapi.exceptions.PaymentNotFound;
import com.marbormi.paymentsapi.mappers.PaymentMapper;
import com.marbormi.paymentsapi.services.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.*;

@DisplayName("Payment Controller Unit Test")
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentController paymentController;

    private final UUID id = UUID.fromString("c5003832-7c62-4745-ac01-82bcf69215db");
    private final String email = "payer@email.com";
    private final CurrencyCode currency = CurrencyCode.USD;
    private final BigDecimal amount = new BigDecimal("22.0");


    @DisplayName("Get all Payments")
    @Test
    void getAllPayments() {
        final Pageable pageable = PageRequest.of(0, 25);
        final List<Payment> payments = List.of(new Payment(
                id,
                LocalDateTime.now(),
                email,
                PaymentStatus.UNPAID,
                currency,
                amount,
                null
        ));
        final Page<Payment> expectedPage = new PageImpl<>(payments, pageable, 1);

        when(paymentService.findAll(pageable)).thenReturn(expectedPage);
        when(paymentMapper.toPaymentDto(any())).thenAnswer(i -> getPaymentDto(i.getArgument(0)));

        assertThat(paymentController.getAllPayments(pageable)).isEqualTo(expectedPage.map(this::getPaymentDto));
    }

    private PaymentDTO getPaymentDto(Payment payment) {
        return new PaymentDTO(
                payment.getId(),
                payment.getCreatedDate(),
                payment.getPayerEmail(),
                payment.getStatus(),
                payment.getCurrency(),
                payment.getAmount(),
                payment.getPaidDate()
        );
    }

    @DisplayName("Get Existent Payment")
    @Test
    void getPayment() {
        final LocalDateTime createdDate = LocalDateTime.of(2023, 1, 30, 10, 1);
        final Payment payment = new Payment(
                id,
                createdDate,
                email,
                PaymentStatus.UNPAID,
                currency,
                amount,
                null
        );
        final var paymentDto = getPaymentDto(payment);

        when(paymentService.findById(id)).thenReturn(payment);
        when(paymentMapper.toPaymentDto(payment)).thenReturn(paymentDto);

        assertThat(paymentController.getPayment(id)).isEqualTo(paymentDto);
    }

    @DisplayName("Get Non-Existent Payment")
    @Test
    public void getNonExistentPayment() {
        when(paymentService.findById(id)).thenThrow(new PaymentNotFound(id));

        final Throwable thrown = catchThrowable(() -> paymentController.getPayment(id));

        assertThat(thrown).isInstanceOf(PaymentNotFound.class);
    }

    @DisplayName("Create Payment")
    @Test
    void createPayment() {
        final Payment payment = Payment.builder()
                .id(id)
                .payerEmail(email)
                .currency(currency)
                .amount(amount)
                .status(PaymentStatus.UNPAID)
                .paidDate(null)
                .build();
        final PaymentCreationDTO paymentCreationDto = new PaymentCreationDTO(
                email,
                currency,
                amount
        );
        final PaymentDTO expectedPaymentDto = getPaymentDto(payment);

        when(paymentMapper.toPayment(paymentCreationDto)).thenReturn(payment);
        when(paymentService.save(payment)).thenReturn(payment);
        when(paymentMapper.toPaymentDto(payment)).thenReturn(expectedPaymentDto);

        assertThat(paymentController.createPayment(paymentCreationDto)).isEqualTo(expectedPaymentDto);
    }

    @DisplayName("Mark as paid")
    @Test
    void markPaymentAsPaid() {
        final LocalDateTime paidAt = LocalDateTime.parse("2023-01-01T01:00:05");
        final Payment payment = Payment.builder()
                .id(id)
                .status(PaymentStatus.PAID)
                .paidDate(paidAt)
                .build();

        when(paymentService.markAsPaid(id)).thenReturn(payment);
        when(paymentMapper.toPaymentDto(payment)).thenReturn(getPaymentDto(payment));

        final PaymentDTO paymentDTO = paymentController.markPaymentAsPaid(id);

        assertThat(paymentDTO.status()).isEqualTo(PaymentStatus.PAID);
        assertThat(paymentDTO.paidDate()).isEqualTo(paidAt);
    }

    @DisplayName("Delete unpaid payment")
    @Test
    void deleteUnpaidPayment(){
        when(paymentService.deleteIfUnpaid(id)).thenReturn(true);

        paymentController.deletePaymentIfUnpaid(id);

        verify(paymentService, times(1)).deleteIfUnpaid(id);
    }

    @DisplayName("Try to delete paid payment")
    @Test
    void deletePaidPayment(){
        when(paymentService.deleteIfUnpaid(id)).thenReturn(false);

        final Throwable thrown = catchThrowable(() -> paymentController.deletePaymentIfUnpaid(id));

        assertThat(thrown).isInstanceOf(PaymentAlreadyPaid.class);
    }
}