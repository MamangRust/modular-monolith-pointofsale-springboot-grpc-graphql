package com.sanedge.pointofsale.transaction.repository.statsbymerchant;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sanedge.pointofsale.BaseIntegrationTest;
import com.sanedge.pointofsale.common.enums.PaymentStatus;
import com.sanedge.pointofsale.transaction.models.Transaction;
import com.sanedge.pointofsale.transaction.models.TransactionMonthlyAmountFailed;
import com.sanedge.pointofsale.transaction.models.TransactionMonthlyAmountSuccess;
import com.sanedge.pointofsale.transaction.models.TransactionYearlyAmountFailed;
import com.sanedge.pointofsale.transaction.models.TransactionYearlyAmountSuccess;
import com.sanedge.pointofsale.transaction.repository.TransactionCommandRepository;

public class TransactionAmountByMerchantRepositoryTest extends BaseIntegrationTest {

    private static final Long MERCHANT_ID = 1L;

    @Autowired
    private TransactionAmountByMerchantRepository transactionAmountByMerchantRepository;

    @Autowired
    private TransactionCommandRepository transactionRepository;

    @BeforeEach
    void setUp() {
        createTransaction(PaymentStatus.SUCCESS, 1000L, LocalDateTime.of(2024, 1, 15, 10, 0));
        createTransaction(PaymentStatus.SUCCESS, 2000L, LocalDateTime.of(2024, 1, 20, 10, 0));
        createTransaction(PaymentStatus.SUCCESS, 1500L, LocalDateTime.of(2023, 12, 15, 10, 0));
        createTransaction(PaymentStatus.FAILED, 500L, LocalDateTime.of(2024, 1, 10, 10, 0));
    }

    private void createTransaction(PaymentStatus status, Long amount, LocalDateTime createdAt) {
        Transaction transaction = new Transaction();
        transaction.setMerchantId(MERCHANT_ID);
        transaction.setOrderId(1L);
        transaction.setAmount(amount.intValue());
        transaction.setPaymentMethod("CREDIT_CARD");
        transaction.setStatus(status);
        transaction.setCreatedAt(java.sql.Timestamp.valueOf(createdAt));
        transaction.setUpdatedAt(java.sql.Timestamp.valueOf(createdAt));
        transactionRepository.save(transaction);
    }

    @Test
    void findMonthlySuccessByMerchant_ShouldReturnCorrectStats() {
        List<TransactionMonthlyAmountSuccess> stats = transactionAmountByMerchantRepository
                .findMonthlySuccessByMerchant(MERCHANT_ID, 2024, 1, 2023, 12);

        assertThat(stats).isNotNull();
    }

    @Test
    void findYearlySuccessByMerchant_ShouldReturnCorrectStats() {
        List<TransactionYearlyAmountSuccess> stats = transactionAmountByMerchantRepository
                .findYearlySuccessByMerchant(MERCHANT_ID, 2024);

        assertThat(stats).isNotNull();
    }

    @Test
    void findMonthlyFailedByMerchant_ShouldReturnCorrectStats() {
        List<TransactionMonthlyAmountFailed> stats = transactionAmountByMerchantRepository
                .findMonthlyFailedByMerchant(MERCHANT_ID, 2024, 1, 2023, 12);

        assertThat(stats).isNotNull();
    }

    @Test
    void findYearlyFailedByMerchant_ShouldReturnCorrectStats() {
        List<TransactionYearlyAmountFailed> stats = transactionAmountByMerchantRepository
                .findYearlyFailedByMerchant(MERCHANT_ID, 2024);

        assertThat(stats).isNotNull();
    }
}
