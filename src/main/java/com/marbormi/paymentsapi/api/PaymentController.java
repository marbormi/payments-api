package com.marbormi.paymentsapi.api;

import com.marbormi.paymentsapi.dtos.PaymentCreationDTO;
import com.marbormi.paymentsapi.dtos.PaymentDTO;
import com.marbormi.paymentsapi.exceptions.PaymentAlreadyPaid;
import com.marbormi.paymentsapi.mappers.PaymentMapper;
import com.marbormi.paymentsapi.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RestController
@RequestMapping("/payments/")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @GetMapping
    public Page<PaymentDTO> getAllPayments(@PageableDefault(size = 20) final Pageable pageable){
        return paymentService.findAll(pageable).map(paymentMapper::toPaymentDto);
    }

    @GetMapping("{id}")
    public PaymentDTO getPayment(@PathVariable UUID id){
        return paymentMapper.toPaymentDto(paymentService.findById(id));
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDTO createPayment(@Valid @RequestBody PaymentCreationDTO paymentCreationDTO) {
        return paymentMapper.toPaymentDto(
                paymentService.save(
                        paymentMapper.toPayment(paymentCreationDTO)
                )
        );
    }

    @PatchMapping("{id}/paid")
    public PaymentDTO markPaymentAsPaid(@PathVariable UUID id){
        return paymentMapper.toPaymentDto(
                paymentService.markAsPaid(id)
        );
    }
}
