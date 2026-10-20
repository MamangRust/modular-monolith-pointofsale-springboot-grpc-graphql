package com.sanedge.pointofsale.order.domain.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MonthTotalRevenueMerchantRequest {

    @NotNull(message = "ID merchant wajib diisi")
    private Integer merchantId;

    @NotNull(message = "Tahun wajib diisi")
    @Min(value = 1900, message = "Tahun harus valid")
    private Integer year;

    @NotNull(message = "Bulan wajib diisi")
    @Min(value = 1, message = "Bulan harus antara 1–12")
    @Max(value = 12, message = "Bulan harus antara 1–12")
    private Integer month;
}
