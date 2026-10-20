package com.sanedge.pointofsale.common.adapter.cashier;

import com.sanedge.pointofsale.common.adapter.model.Cashier;

/**
 * Port for the cashier query service, replacing direct
 * {@code @GrpcClient("cashier")} usage in consumer services.
 */
public interface CashierQueryPort {

    Cashier findById(int cashierId);
}
