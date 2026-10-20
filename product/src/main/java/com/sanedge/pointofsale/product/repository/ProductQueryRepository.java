package com.sanedge.pointofsale.product.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.product.models.Product;

@Repository
public interface ProductQueryRepository extends JpaRepository<Product, Long> {
    @Query("""
            SELECT p FROM Product p
            WHERE (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY p.createdAt ASC
            """)
    Page<Product> findAllProducts(@Param("search") String search, Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE p.deletedAt IS NULL
            AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY p.createdAt ASC
            """)
    Page<Product> findActiveProducts(@Param("search") String search, Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE p.deletedAt IS NOT NULL
            AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY p.deletedAt DESC
            """)
    Page<Product> findTrashedProducts(@Param("search") String search, Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE p.productId = :productId
            """)
    Optional<Product> findProductById(@Param("productId") Long productId);

    @Query("""
            SELECT p FROM Product p
            WHERE p.deletedAt IS NULL
            AND p.merchantId = :merchantId
            AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:categoryId IS NULL OR p.categoryId = :categoryId)
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            ORDER BY p.createdAt ASC
            """)
    Page<Product> findProductsByMerchant(
            @Param("merchantId") Long merchantId,
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE p.deletedAt IS NULL
            AND p.categoryId = :categoryId
            AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            ORDER BY p.createdAt ASC
            """)
    Page<Product> findProductsByCategoryId(
            @Param("categoryId") Long categoryId,
            @Param("search") String search,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            Pageable pageable);
}
