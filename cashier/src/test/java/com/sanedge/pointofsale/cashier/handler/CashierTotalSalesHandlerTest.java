package com.sanedge.pointofsale.cashier.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthTotalSales;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearTotalSales;
import com.sanedge.pointofsale.cashier.service.stats.CashierTotalSalesService;
import com.sanedge.pointofsale.cashier.service.statsbyid.CashierTotalSalesByIdService;
import com.sanedge.pointofsale.cashier.service.statsbymerchant.CashierTotalSalesByMerchantService;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashierMonthTotalSales;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashierYearTotalSales;
import com.sanedge.pointofsale.pb.cashier.FindYearMonthTotalSales;
import com.sanedge.pointofsale.pb.cashier.FindYearMonthTotalSalesByMerchant;
import com.sanedge.pointofsale.pb.cashier.FindYearMonthTotalSalesById;
import com.sanedge.pointofsale.pb.cashier.FindYearTotalSales;
import com.sanedge.pointofsale.pb.cashier.FindYearTotalSalesByMerchant;
import com.sanedge.pointofsale.pb.cashier.FindYearTotalSalesById;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

@ExtendWith(MockitoExtension.class)
class CashierTotalSalesHandlerTest {

    @Mock
    private CashierTotalSalesService cashierTotalSalesService;
    @Mock
    private CashierTotalSalesByIdService cashierTotalSalesByIdService;
    @Mock
    private CashierTotalSalesByMerchantService cashierTotalSalesByMerchantService;

    @InjectMocks
    private CashierTotalSalesHandler handler;

    private static CashierResponseMonthTotalSales monthTotal() {
        return CashierResponseMonthTotalSales.builder()
                .year("2024").month("2024-01").totalSales(5000L).build();
    }

    private static CashierResponseYearTotalSales yearTotal() {
        return CashierResponseYearTotalSales.builder().year("2024").totalSales(60000L).build();
    }

    @Test
    void findMonthlyTotalSales_mapsList() {
        when(cashierTotalSalesService.findMonthlyTotalSales(any()))
                .thenReturn(ApiResponse.<List<CashierResponseMonthTotalSales>>builder()
                        .status("success").message("ok").data(List.of(monthTotal())).build());

        TestStreamObserver<ApiResponseCashierMonthTotalSales> observer = new TestStreamObserver<>();
        handler.findMonthlyTotalSales(
                FindYearMonthTotalSales.newBuilder().setYear(2024).setMonth(1).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.isCompleted()).isTrue();
        assertThat(observer.value().getStatus()).isEqualTo("success");
        assertThat(observer.value().getData(0).getTotalSales()).isEqualTo(5000L);
    }

    @Test
    void findYearlyTotalSales_mapsList() {
        when(cashierTotalSalesService.findYearlyTotalSales(2024))
                .thenReturn(ApiResponse.<List<CashierResponseYearTotalSales>>builder()
                        .status("success").message("ok").data(List.of(yearTotal())).build());

        TestStreamObserver<ApiResponseCashierYearTotalSales> observer = new TestStreamObserver<>();
        handler.findYearlyTotalSales(FindYearTotalSales.newBuilder().setYear(2024).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData(0).getYear()).isEqualTo("2024");
    }

    @Test
    void findMonthlyTotalSalesById_mapsList() {
        when(cashierTotalSalesByIdService.findMonthlyTotalSalesById(any()))
                .thenReturn(ApiResponse.<List<CashierResponseMonthTotalSales>>builder()
                        .status("success").message("ok").data(List.of(monthTotal())).build());

        TestStreamObserver<ApiResponseCashierMonthTotalSales> observer = new TestStreamObserver<>();
        handler.findMonthlyTotalSalesById(
                FindYearMonthTotalSalesById.newBuilder().setCashierId(1).setYear(2024).setMonth(1).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData(0).getMonth()).isEqualTo("2024-01");
    }

    @Test
    void findYearlyTotalSalesById_mapsList() {
        when(cashierTotalSalesByIdService.findYearlyTotalSalesById(any()))
                .thenReturn(ApiResponse.<List<CashierResponseYearTotalSales>>builder()
                        .status("success").message("ok").data(List.of(yearTotal())).build());

        TestStreamObserver<ApiResponseCashierYearTotalSales> observer = new TestStreamObserver<>();
        handler.findYearlyTotalSalesById(
                FindYearTotalSalesById.newBuilder().setCashierId(1).setYear(2024).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData(0).getTotalSales()).isEqualTo(60000L);
    }

    @Test
    void findMonthlyTotalSalesByMerchant_mapsList() {
        when(cashierTotalSalesByMerchantService.findMonthlyTotalSalesByMerchant(any()))
                .thenReturn(ApiResponse.<List<CashierResponseMonthTotalSales>>builder()
                        .status("success").message("ok").data(List.of(monthTotal())).build());

        TestStreamObserver<ApiResponseCashierMonthTotalSales> observer = new TestStreamObserver<>();
        handler.findMonthlyTotalSalesByMerchant(
                FindYearMonthTotalSalesByMerchant.newBuilder().setMerchantId(2).setYear(2024).setMonth(1).build(),
                observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getDataCount()).isEqualTo(1);
    }

    @Test
    void findYearlyTotalSalesByMerchant_mapsList() {
        when(cashierTotalSalesByMerchantService.findYearlyTotalSalesByMerchant(any()))
                .thenReturn(ApiResponse.<List<CashierResponseYearTotalSales>>builder()
                        .status("success").message("ok").data(List.of(yearTotal())).build());

        TestStreamObserver<ApiResponseCashierYearTotalSales> observer = new TestStreamObserver<>();
        handler.findYearlyTotalSalesByMerchant(
                FindYearTotalSalesByMerchant.newBuilder().setMerchantId(2).setYear(2024).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData(0).getYear()).isEqualTo("2024");
    }

    @Test
    void findYearlyTotalSales_whenServiceThrows_propagatesNotFound() {
        when(cashierTotalSalesService.findYearlyTotalSales(2024))
                .thenThrow(new ResourceNotFoundException("total sales missing"));

        TestStreamObserver<ApiResponseCashierYearTotalSales> observer = new TestStreamObserver<>();
        handler.findYearlyTotalSales(FindYearTotalSales.newBuilder().setYear(2024).build(), observer);

        assertThat(observer.value()).isNull();
        assertThat(observer.error()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException ex = (StatusRuntimeException) observer.error();
        assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
        assertThat(ex.getStatus().getDescription()).isEqualTo("total sales missing");
    }
}
