package com.sanedge.pointofsale.merchant.domain.requests;

import jakarta.validation.constraints.Min;
import lombok.Data;

/** Port of the blueprint's {@code FindAllMerchantDocuments} (gRPC page request). */
@Data
public class FindAllMerchantDocuments {

    @Min(value = 1, message = "Page minimal 1")
    private Integer page = 1;

    @Min(value = 1, message = "Page size minimal 1")
    private Integer pageSize = 10;

    private String search = "";
}
