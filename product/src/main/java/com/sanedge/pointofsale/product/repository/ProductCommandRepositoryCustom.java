package com.sanedge.pointofsale.product.repository;

import com.sanedge.pointofsale.product.models.Product;

public interface ProductCommandRepositoryCustom {
    Product trashed(Long productId);

    Product restore(Long productId);

    Product deletePermanent(Long productId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();
}