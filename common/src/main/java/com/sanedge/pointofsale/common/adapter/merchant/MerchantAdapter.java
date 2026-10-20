package com.sanedge.pointofsale.common.adapter.merchant;

import com.sanedge.pointofsale.common.adapter.model.Merchant;
import com.sanedge.pointofsale.common.adapter.support.ProtoTime;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.pb.merchant.FindByIdMerchantRequest;
import com.sanedge.pointofsale.pb.merchant.MerchantQueryServiceGrpc.MerchantQueryServiceBlockingStub;
import com.sanedge.pointofsale.pb.merchant.MerchantResponse;

public class MerchantAdapter implements MerchantQueryPort {

    private final MerchantQueryServiceBlockingStub query;

    public MerchantAdapter(MerchantQueryServiceBlockingStub query) {
        this.query = query;
    }

    @Override
    public Merchant findByMerchantId(int merchantId) {
        var resp = query.findById(FindByIdMerchantRequest.newBuilder().setMerchantId(merchantId).build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Merchant not found: " + merchantId);
        }
        return toMerchant(resp.getData());
    }

    private static Merchant toMerchant(MerchantResponse m) {
        if (m == null) {
            return null;
        }
        // The Spring merchant proto exposes no api_key field; the Quarkus source
        // mapped one, so it is carried as null here.
        return new Merchant(m.getMerchantId(), m.getName(), null, m.getStatus(), m.getUserId(),
                ProtoTime.parse(m.getCreatedAt()), ProtoTime.parse(m.getUpdatedAt()));
    }
}
