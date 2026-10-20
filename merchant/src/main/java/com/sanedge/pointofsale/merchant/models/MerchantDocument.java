package com.sanedge.pointofsale.merchant.models;

import com.sanedge.pointofsale.common.models.BaseModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Merchant verification document (table {@code merchant_documents}, migration
 * V12).
 *
 * <p>Port of the blueprint's {@code MerchantDocument} entity. The source project
 * shipped the entity/protos but no migration, so the table is new here.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "merchant_documents")
public class MerchantDocument extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "merchant_id", nullable = false)
    private Integer merchantId;

    @Column(name = "document_type", nullable = false, length = 100)
    private String documentType;

    @Column(name = "document_url", nullable = false, columnDefinition = "TEXT")
    private String documentUrl;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String note;
}
