package com.marbormi.paymentsapi.services;

import com.marbormi.paymentsapi.domain.Payment;
import com.marbormi.paymentsapi.domain.PaymentStatus;
import com.marbormi.paymentsapi.repositories.PaymentRepository;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.*;

@DisplayName("PaymentServiceImpl UnitTest")
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final UUID id = UUID.fromString("8efabf26-c103-4b9e-b24e-5bf61608229b");

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Find all")
    void findAll() {
        final Pageable pageable = PageRequest.of(0, 25);
        final List<Payment> payments = new ArrayList<>();
        final Page<Payment> expectedPage = new PageImpl<>(payments, pageable, 0);

        when(paymentRepository.findAll(pageable)).thenReturn(expectedPage);

        assertThat(paymentService.findAll(pageable))
                .isEqualTo(expectedPage);
    }

    @Test
    @DisplayName("Find by id")
    void findById() {
        final Payment payment = new Payment();

        when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));

        assertThat(paymentService.findById(id)).isEqualTo(payment);
    }

    @Test
    @DisplayName("Save")
    void save() {
        final Payment payment = new Payment();

        when(paymentRepository.save(payment)).thenReturn(payment);

        assertThat(paymentService.save(payment)).isEqualTo(payment);
    }
    @DisplayName("Delete unpaid payment")
    @Test
    void deleteIfUnpaid() {
        final Payment payment = new Payment();

        when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));

        paymentService.deleteIfUnpaid(id);

        verify(paymentRepository, times(1)).delete(payment);
    }

    @DisplayName("Trying to delete paid payment")
    @Test
    void deletePaid() {
        final Payment payment = Payment.builder()
                .status(PaymentStatus.PAID)
                .build();
        when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));

        paymentService.deleteIfUnpaid(id);

        verify(paymentRepository, never()).delete(payment);
    }

    @DisplayName("Mark payment as paid")
    @Test
    void markAsPaid() {
        final Payment payment = new Payment();

        when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));
        when(paymentService.save(payment)).thenReturn(payment);

        paymentService.markAsPaid(id);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaidDate()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
    }

    @DisplayName("Try to mark payment as paid N time")
    @Test
    void markAsPaidAgain() {
        final LocalDateTime paidAt = LocalDateTime.of(2010, 6, 11, 22, 58);
        final Payment payment = Payment.builder()
                .status(PaymentStatus.PAID)
                .paidDate(paidAt)
                .build();

        when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));

        paymentService.markAsPaid(id);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaidDate()).isEqualTo(paidAt);

        verify(paymentRepository, never()).save(payment);
    }
}