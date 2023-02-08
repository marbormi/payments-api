package com.marbormi.paymentsapi.repositories;

import com.marbormi.paymentsapi.domain.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional
@Sql("PaymentRepositoryTestIT.sql")
@DisplayName("PaymentRepositoryTest IT")
class PaymentRepositoryTest {

    @Autowired
    PaymentRepository paymentRepository;

    private static final int pageSize = 4;
    private static final List<UUID> firstPageExpectedIds = List.of(
            UUID.fromString("c5003832-7c62-4745-ac01-82bcf69215db"),
            UUID.fromString("e8fca603-9361-40a9-a1db-ac0b4b2d2b02"),
            UUID.fromString("cd927c6b-eaf2-4ca3-8b82-507861ccf178"),
            UUID.fromString("efeb487a-be68-4999-ac1f-278396335b47")
    );

    private static final List<UUID> lastPageExpectedIds = List.of(
            UUID.fromString("a48c0b1c-8093-42a1-92f5-e42e682e8730"),
            UUID.fromString("ee07a762-8767-4f23-8a3e-c7542ba09228")
    );

    @Test
    void findAll() {
        assertPayments(
                paymentRepository.findAll(PageRequest.of(0, pageSize)),
                firstPageExpectedIds
        );

        assertPayments(
                paymentRepository.findAll(PageRequest.of(1, pageSize)),
                lastPageExpectedIds
        );
    }

    void assertPayments(Page<Payment> payments, List<UUID> expectedIds) {
        assertThat(payments.getTotalPages()).isEqualTo(2);
        assertThat(payments.getContent())
                .extracting(Payment::getId)
                .containsExactlyElementsOf(expectedIds);
    }
}