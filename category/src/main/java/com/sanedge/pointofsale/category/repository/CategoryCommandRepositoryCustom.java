package com.sanedge.pointofsale.category.repository;

import com.sanedge.pointofsale.category.models.Category;

public interface CategoryCommandRepositoryCustom {
    Category trashed(Long categoryId);

    Category restore(Long categoryId);

    Category deletePermanent(Long categoryId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();
}