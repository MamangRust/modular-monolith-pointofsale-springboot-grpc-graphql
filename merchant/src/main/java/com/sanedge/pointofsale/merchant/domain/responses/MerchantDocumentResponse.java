package com.sanedge.pointofsale.merchant.domain.responses;

import com.sanedge.pointofsale.merchant.models.MerchantDocument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Port of the blueprint's {@code MerchantDocumentResponse}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDocumentResponse {
    private Long documentId;
    private Long merchantId;
    private String documentType;
    private String documentUrl;
    private String status;
    private String note;
    private String createdAt;
    private String updatedAt;

    public static MerchantDocumentResponse from(MerchantDocument doc) {
        if (doc == null) {
            return null;
        }
        return MerchantDocumentResponse.builder()
                .documentId(doc.getDocumentId())
                .merchantId(doc.getMerchantId() == null ? null : doc.getMerchantId().longValue())
                .documentType(doc.getDocumentType())
                .documentUrl(doc.getDocumentUrl())
                .status(doc.getStatus())
                .note(doc.getNote())
                .createdAt(doc.getCreatedAt() != null ? doc.getCreatedAt().toString() : null)
                .updatedAt(doc.getUpdatedAt() != null ? doc.getUpdatedAt().toString() : null)
                .build();
    }
}
