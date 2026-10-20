package com.sanedge.pointofsale.merchant.repository;

import com.sanedge.pointofsale.merchant.models.Merchant;

public interface MerchantCommandRepositoryCustom {
    Merchant trashed(Long merchantId);

    Merchant restore(Long merchantId);

    boolean deletePermanent(Long merchantId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();
}