package com.sanedge.pointofsale.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.category.models.Category;

@Repository
public interface CategoryCommandRepository extends JpaRepository<Category, Long>, CategoryCommandRepositoryCustom {
}
