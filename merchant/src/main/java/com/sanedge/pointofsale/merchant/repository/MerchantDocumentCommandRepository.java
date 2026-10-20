package com.sanedge.pointofsale.merchant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.merchant.models.MerchantDocument;

/** Command side for merchant documents; soft-delete ops live in the custom impl. */
@Repository
public interface MerchantDocumentCommandRepository
        extends JpaRepository<MerchantDocument, Long>, MerchantDocumentCommandRepositoryCustom {

}
