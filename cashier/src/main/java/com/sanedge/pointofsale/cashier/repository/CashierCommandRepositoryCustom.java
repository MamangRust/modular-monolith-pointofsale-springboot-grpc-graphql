package com.sanedge.pointofsale.cashier.repository;

import com.sanedge.pointofsale.cashier.models.Cashier;

public interface CashierCommandRepositoryCustom {
    Cashier trashed(Long cashierId);

    Cashier restore(Long cashierId);

    Cashier deletePermanent(Long cashierId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();
}
