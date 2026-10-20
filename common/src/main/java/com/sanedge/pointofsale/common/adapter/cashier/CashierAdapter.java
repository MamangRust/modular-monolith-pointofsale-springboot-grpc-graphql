package com.sanedge.pointofsale.common.adapter.cashier;

import com.sanedge.pointofsale.common.adapter.model.Cashier;
import com.sanedge.pointofsale.common.adapter.support.ProtoTime;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.pb.cashier.CashierQueryServiceGrpc.CashierQueryServiceBlockingStub;
import com.sanedge.pointofsale.pb.cashier.CashierResponse;
import com.sanedge.pointofsale.pb.cashier.FindByIdCashierRequest;

public class CashierAdapter implements CashierQueryPort {

    private final CashierQueryServiceBlockingStub query;

    public CashierAdapter(CashierQueryServiceBlockingStub query) {
        this.query = query;
    }

    @Override
    public Cashier findById(int cashierId) {
        var resp = query.findById(FindByIdCashierRequest.newBuilder().setCashierId(cashierId).build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Cashier not found: " + cashierId);
        }
        return toCashier(resp.getData());
    }

    private static Cashier toCashier(CashierResponse c) {
        if (c == null) {
            return null;
        }
        return new Cashier(c.getCashierId(), c.getMerchantId(), c.getName(),
                ProtoTime.parse(c.getCreatedAt()), ProtoTime.parse(c.getUpdatedAt()));
    }
}
