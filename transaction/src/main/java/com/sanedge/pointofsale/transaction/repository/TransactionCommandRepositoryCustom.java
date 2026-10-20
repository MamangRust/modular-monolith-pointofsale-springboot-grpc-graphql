package com.sanedge.pointofsale.transaction.repository;

import com.sanedge.pointofsale.transaction.models.Transaction;

public interface TransactionCommandRepositoryCustom {
    Transaction trashed(Long transactionId);

    Transaction restore(Long transactionId);

    Transaction deletePermanent(Long transactionId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();
}