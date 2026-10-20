package com.sanedge.pointofsale.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.product.models.Product;

@Repository
public interface ProductCommandRepository extends JpaRepository<Product, Long>, ProductCommandRepositoryCustom {
}