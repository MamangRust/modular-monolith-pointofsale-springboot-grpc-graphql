package com.sanedge.pointofsale.merchant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.merchant.models.Merchant;

@Repository
public interface MerchantCommandRepository extends JpaRepository<Merchant, Long>, MerchantCommandRepositoryCustom {
}