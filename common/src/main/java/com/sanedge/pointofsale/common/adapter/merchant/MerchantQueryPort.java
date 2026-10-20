package com.sanedge.pointofsale.common.adapter.merchant;

import com.sanedge.pointofsale.common.adapter.model.Merchant;

/**
 * Port for the merchant query service, replacing direct
 * {@code @GrpcClient("merchant")} usage in consumer services.
 */
public interface MerchantQueryPort {

    Merchant findByMerchantId(int merchantId);
}
