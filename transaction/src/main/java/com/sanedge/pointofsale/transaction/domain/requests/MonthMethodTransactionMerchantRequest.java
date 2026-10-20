package com.sanedge.pointofsale.transaction.domain.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MonthMethodTransactionMerchantRequest {

    @NotNull(message = "Merchant ID wajib diisi")
    private Integer merchantId;

    @NotNull(message = "Tahun wajib diisi")
    @Min(value = 1900, message = "Tahun harus valid")
    private Integer year;

    @NotNull(message = "Bulan wajib diisi")
    @Min(value = 1, message = "Bulan harus antara 1 sampai 12")
    private Integer month;
}
