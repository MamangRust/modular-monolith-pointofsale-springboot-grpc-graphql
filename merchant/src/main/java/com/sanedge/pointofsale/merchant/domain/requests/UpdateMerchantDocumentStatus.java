package com.sanedge.pointofsale.merchant.domain.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Port of the blueprint's {@code UpdateMerchantDocumentStatus}. */
@Data
public class UpdateMerchantDocumentStatus {

    @Min(value = 1, message = "documentId minimal 1")
    private Long documentId;

    @Min(value = 1, message = "merchantId minimal 1")
    private Long merchantId;

    private String note;

    @NotBlank(message = "Status wajib diisi")
    private String status;
}
