package com.sanedge.pointofsale.transaction.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.sanedge.pointofsale.BaseIntegrationTest;
import com.sanedge.pointofsale.common.enums.PaymentStatus;
import com.sanedge.pointofsale.transaction.models.Transaction;

public class TransactionRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionQueryRepository queryRepository;

    @Autowired
    private TransactionCommandRepository commandRepository;

    @Test
    void shouldCreateAndQueryTransactions() {
        Transaction tx = new Transaction();
        tx.setOrderId(9999L);
        tx.setMerchantId(2L);
        tx.setAmount(10000);
        tx.setPaymentMethod("OVO");
        tx.setStatus(PaymentStatus.SUCCESS);
        tx = commandRepository.save(tx);

        entityManager.flush();
        entityManager.clear();

        Page<Transaction> page = queryRepository.findTransactions("OVO", PageRequest.of(0, 10));
        assertThat(page.getContent()).isNotEmpty();

        Optional<Transaction> fetched = queryRepository.findByTransactionId(tx.getTransactionId());
        assertThat(fetched).isPresent();

        Optional<Transaction> fetchedByOrder = queryRepository.findByOrderId(9999L);
        assertThat(fetchedByOrder).isPresent();

        Transaction trashed = commandRepository.trashed(tx.getTransactionId());
        assertThat(trashed).isNotNull();

        Transaction restored = commandRepository.restore(tx.getTransactionId());
        assertThat(restored).isNotNull();

        Transaction deleted = commandRepository.deletePermanent(tx.getTransactionId());
        assertThat(deleted).isNotNull();
    }
}
