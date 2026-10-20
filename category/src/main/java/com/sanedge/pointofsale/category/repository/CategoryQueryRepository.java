package com.sanedge.pointofsale.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.category.models.Category;

@Repository
public interface CategoryQueryRepository extends JpaRepository<Category, Long> {
        @Query("""
                        SELECT c FROM Category c
                        WHERE (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                        ORDER BY c.createdAt ASC
                        """)
        Page<Category> findCategories(@Param("keyword") String keyword, Pageable pageable);

        @Query("""
                        SELECT c FROM Category c
                        WHERE c.deletedAt IS NULL
                        AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                        ORDER BY c.createdAt ASC
                        """)
        Page<Category> findActiveCategories(@Param("keyword") String keyword, Pageable pageable);

        @Query("""
                        SELECT c FROM Category c
                        WHERE c.deletedAt IS NOT NULL
                        AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                        ORDER BY c.deletedAt DESC
                        """)
        Page<Category> findTrashedCategories(@Param("keyword") String keyword, Pageable pageable);

        @Query("""
                        SELECT new com.sanedge.pointofsale.category.models.Category(c.categoryId, c.name, null, null)
                        FROM Category c
                        WHERE c.deletedAt IS NULL
                        ORDER BY c.name ASC
                        """)
        List<Category> findNameAndId();

        Optional<Category> findByName(String name);

        @Query("""
                            SELECT c
                            FROM Category c
                            WHERE c.categoryId = :categoryId
                              AND c.deletedAt IS NULL
                        """)
        Optional<Category> findCategoryById(@Param("categoryId") Long categoryId);

}
