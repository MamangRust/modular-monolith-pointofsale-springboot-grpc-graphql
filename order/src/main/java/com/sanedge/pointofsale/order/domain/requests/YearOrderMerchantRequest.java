package com.sanedge.pointofsale.order.domain.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class YearOrderMerchantRequest {

    @NotNull(message = "ID merchant wajib diisi")
    private Integer merchantId;

    @NotNull(message = "Tahun wajib diisi")
    private Integer year;
}
