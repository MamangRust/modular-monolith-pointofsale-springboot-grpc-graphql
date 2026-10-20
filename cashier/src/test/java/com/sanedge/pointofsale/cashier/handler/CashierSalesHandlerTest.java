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

import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthSales;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearSales;
import com.sanedge.pointofsale.cashier.service.stats.CashierSalesService;
import com.sanedge.pointofsale.cashier.service.statsbyid.CashierSalesByIdService;
import com.sanedge.pointofsale.cashier.service.statsbymerchant.CashierSalesByMerchantService;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashierMonthSales;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashierYearSales;
import com.sanedge.pointofsale.pb.cashier.FindYearCashier;
import com.sanedge.pointofsale.pb.cashier.FindYearCashierByMerchant;
import com.sanedge.pointofsale.pb.cashier.FindYearCashierById;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

@ExtendWith(MockitoExtension.class)
class CashierSalesHandlerTest {

    @Mock
    private CashierSalesService cashierSalesService;
    @Mock
    private CashierSalesByIdService cashierSalesByIdService;
    @Mock
    private CashierSalesByMerchantService cashierSalesByMerchantService;

    @InjectMocks
    private CashierSalesHandler handler;

    private static CashierResponseMonthSales monthSales() {
        return CashierResponseMonthSales.builder()
                .month("2024-01").cashierId(1).cashierName("Cashier One")
                .orderCount(5).totalSales(1000L).build();
    }

    private static CashierResponseYearSales yearSales() {
        return CashierResponseYearSales.builder()
                .year("2024").cashierId(1).cashierName("Cashier One")
                .orderCount(50).totalSales(10000L).build();
    }

    @Test
    void findMonthSales_mapsList() {
        when(cashierSalesService.findMonthlySales(2024))
                .thenReturn(ApiResponse.<List<CashierResponseMonthSales>>builder()
                        .status("success").message("ok").data(List.of(monthSales())).build());

        TestStreamObserver<ApiResponseCashierMonthSales> observer = new TestStreamObserver<>();
        handler.findMonthSales(FindYearCashier.newBuilder().setYear(2024).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.isCompleted()).isTrue();
        assertThat(observer.value().getStatus()).isEqualTo("success");
        assertThat(observer.value().getDataCount()).isEqualTo(1);
        assertThat(observer.value().getData(0).getMonth()).isEqualTo("2024-01");
        assertThat(observer.value().getData(0).getTotalSales()).isEqualTo(1000L);
    }

    @Test
    void findYearSales_mapsList() {
        when(cashierSalesService.findYearlySales(2024))
                .thenReturn(ApiResponse.<List<CashierResponseYearSales>>builder()
                        .status("success").message("ok").data(List.of(yearSales())).build());

        TestStreamObserver<ApiResponseCashierYearSales> observer = new TestStreamObserver<>();
        handler.findYearSales(FindYearCashier.newBuilder().setYear(2024).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData(0).getYear()).isEqualTo("2024");
    }

    @Test
    void findMonthSalesById_mapsList() {
        when(cashierSalesByIdService.findMonthlyCashierById(any()))
                .thenReturn(ApiResponse.<List<CashierResponseMonthSales>>builder()
                        .status("success").message("ok").data(List.of(monthSales())).build());

        TestStreamObserver<ApiResponseCashierMonthSales> observer = new TestStreamObserver<>();
        handler.findMonthSalesById(FindYearCashierById.newBuilder().setCashierId(1).setYear(2024).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData(0).getCashierId()).isEqualTo(1);
    }

    @Test
    void findYearSalesById_mapsList() {
        when(cashierSalesByIdService.findYearlyCashierById(any()))
                .thenReturn(ApiResponse.<List<CashierResponseYearSales>>builder()
                        .status("success").message("ok").data(List.of(yearSales())).build());

        TestStreamObserver<ApiResponseCashierYearSales> observer = new TestStreamObserver<>();
        handler.findYearSalesById(FindYearCashierById.newBuilder().setCashierId(1).setYear(2024).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData(0).getTotalSales()).isEqualTo(10000L);
    }

    @Test
    void findMonthSalesByMerchant_mapsList() {
        when(cashierSalesByMerchantService.findMonthlyCashierByMerchant(any()))
                .thenReturn(ApiResponse.<List<CashierResponseMonthSales>>builder()
                        .status("success").message("ok").data(List.of(monthSales())).build());

        TestStreamObserver<ApiResponseCashierMonthSales> observer = new TestStreamObserver<>();
        handler.findMonthSalesByMerchant(
                FindYearCashierByMerchant.newBuilder().setMerchantId(2).setYear(2024).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData(0).getCashierName()).isEqualTo("Cashier One");
    }

    @Test
    void findYearSalesByMerchant_mapsList() {
        when(cashierSalesByMerchantService.findYearlyCashierByMerchant(any()))
                .thenReturn(ApiResponse.<List<CashierResponseYearSales>>builder()
                        .status("success").message("ok").data(List.of(yearSales())).build());

        TestStreamObserver<ApiResponseCashierYearSales> observer = new TestStreamObserver<>();
        handler.findYearSalesByMerchant(
                FindYearCashierByMerchant.newBuilder().setMerchantId(2).setYear(2024).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData(0).getOrderCount()).isEqualTo(50);
    }

    @Test
    void findMonthSales_whenServiceThrows_propagatesNotFound() {
        when(cashierSalesService.findMonthlySales(2024))
                .thenThrow(new ResourceNotFoundException("sales missing"));

        TestStreamObserver<ApiResponseCashierMonthSales> observer = new TestStreamObserver<>();
        handler.findMonthSales(FindYearCashier.newBuilder().setYear(2024).build(), observer);

        assertThat(observer.value()).isNull();
        assertThat(observer.error()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException ex = (StatusRuntimeException) observer.error();
        assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
        assertThat(ex.getStatus().getDescription()).isEqualTo("sales missing");
    }
}
