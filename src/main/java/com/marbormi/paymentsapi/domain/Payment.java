package com.marbormi.paymentsapi.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payment")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
@Builder
public class Payment {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false)
    private UUID id;

    @CreatedDate
    private LocalDateTime createdDate;

    private String payerEmail;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.UNPAID;

    @Enumerated(EnumType.STRING)
    private CurrencyCode currency;

    private BigDecimal amount;

    private LocalDateTime paidDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment payment)) return false;

        if (!Objects.equals(id, payment.id)) return false;
        if (!Objects.equals(createdDate, payment.createdDate)) return false;
        if (!Objects.equals(payerEmail, payment.payerEmail)) return false;
        if (status != payment.status) return false;
        if (currency != payment.currency) return false;
        if (!Objects.equals(amount, payment.amount)) return false;
        return Objects.equals(paidDate, payment.paidDate);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (createdDate != null ? createdDate.hashCode() : 0);
        result = 31 * result + (payerEmail != null ? payerEmail.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (paidDate != null ? paidDate.hashCode() : 0);
        return result;
    }
}
