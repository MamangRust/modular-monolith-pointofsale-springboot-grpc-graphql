package com.sanedge.pointofsale.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.transaction.models.Transaction;

@Repository
public interface TransactionCommandRepository
        extends JpaRepository<Transaction, Long>, TransactionCommandRepositoryCustom {
}