package com.sanedge.pointofsale.cashier.repository;

import com.sanedge.pointofsale.cashier.models.Cashier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashierCommandRepository extends JpaRepository<Cashier, Long>, CashierCommandRepositoryCustom {
}
