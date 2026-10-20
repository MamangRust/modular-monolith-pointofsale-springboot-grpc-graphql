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

import com.sanedge.pointofsale.cashier.domain.requests.FindAllCashierMerchant;
import com.sanedge.pointofsale.cashier.domain.requests.FindAllCashiers;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponse;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseDeleteAt;
import com.sanedge.pointofsale.cashier.service.CashierQueryService;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.domain.responses.api.PaginationMeta;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashier;
import com.sanedge.pointofsale.pb.cashier.ApiResponsePaginationCashier;
import com.sanedge.pointofsale.pb.cashier.ApiResponsePaginationCashierDeleteAt;
import com.sanedge.pointofsale.pb.cashier.FindAllCashierRequest;
import com.sanedge.pointofsale.pb.cashier.FindByMerchantCashierRequest;
import com.sanedge.pointofsale.pb.cashier.FindByIdCashierRequest;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

@ExtendWith(MockitoExtension.class)
class CashierQueryHandlerTest {

    @Mock
    private CashierQueryService service;

    @InjectMocks
    private CashierQueryHandler handler;

    private static PaginationMeta meta() {
        return new PaginationMeta(1, 10, 1, 1);
    }

    private static CashierResponse cashier() {
        return CashierResponse.builder()
                .id(1)
                .merchantId(2)
                .name("Cashier One")
                .createdAt("2024-01-01T00:00:00")
                .updatedAt("2024-01-02T00:00:00")
                .build();
    }

    private static CashierResponseDeleteAt cashierDeleteAt() {
        return CashierResponseDeleteAt.builder()
                .id(1)
                .merchantId(2)
                .name("Cashier One")
                .createdAt("2024-01-01T00:00:00")
                .updatedAt("2024-01-02T00:00:00")
                .deletedAt("2024-01-03T00:00:00")
                .build();
    }

    @Test
    void findAll_mapsPageAndData() {
        when(service.findAll(any(FindAllCashiers.class)))
                .thenReturn(ApiResponsePagination.<List<CashierResponse>>builder()
                        .status("success").message("ok")
                        .data(List.of(cashier())).pagination(meta()).build());

        TestStreamObserver<ApiResponsePaginationCashier> observer = new TestStreamObserver<>();
        handler.findAll(FindAllCashierRequest.newBuilder().setPage(1).setPageSize(10).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.isCompleted()).isTrue();
        ApiResponsePaginationCashier res = observer.value();
        assertThat(res.getStatus()).isEqualTo("success");
        assertThat(res.getMessage()).isEqualTo("ok");
        assertThat(res.getDataCount()).isEqualTo(1);
        assertThat(res.getData(0).getCashierId()).isEqualTo(1);
        assertThat(res.getData(0).getMerchantId()).isEqualTo(2);
        assertThat(res.getData(0).getName()).isEqualTo("Cashier One");
        assertThat(res.getPaginationMeta().getCurrentPage()).isEqualTo(1);
        assertThat(res.getPaginationMeta().getTotalRecords()).isEqualTo(1);
    }

    @Test
    void findById_mapsSingle() {
        when(service.findById(1)).thenReturn(ApiResponse.<CashierResponse>builder()
                .status("success").message("ok").data(cashier()).build());

        TestStreamObserver<ApiResponseCashier> observer = new TestStreamObserver<>();
        handler.findById(FindByIdCashierRequest.newBuilder().setCashierId(1).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getStatus()).isEqualTo("success");
        assertThat(observer.value().getData().getCashierId()).isEqualTo(1);
        assertThat(observer.value().getData().getName()).isEqualTo("Cashier One");
    }

    @Test
    void findByMerchant_mapsPageAndData() {
        when(service.findByMerchant(any(FindAllCashierMerchant.class)))
                .thenReturn(ApiResponsePagination.<List<CashierResponse>>builder()
                        .status("success").message("ok")
                        .data(List.of(cashier())).pagination(meta()).build());

        TestStreamObserver<ApiResponsePaginationCashier> observer = new TestStreamObserver<>();
        handler.findByMerchant(FindByMerchantCashierRequest.newBuilder()
                .setMerchantId(2).setPage(1).setPageSize(10).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getDataCount()).isEqualTo(1);
        assertThat(observer.value().getData(0).getMerchantId()).isEqualTo(2);
    }

    @Test
    void findByActive_mapsPageAndData() {
        when(service.findByActive(any(FindAllCashiers.class)))
                .thenReturn(ApiResponsePagination.<List<CashierResponseDeleteAt>>builder()
                        .status("success").message("ok")
                        .data(List.of(cashierDeleteAt())).pagination(meta()).build());

        TestStreamObserver<ApiResponsePaginationCashierDeleteAt> observer = new TestStreamObserver<>();
        handler.findByActive(FindAllCashierRequest.newBuilder().setPage(1).setPageSize(10).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getDataCount()).isEqualTo(1);
        assertThat(observer.value().getData(0).getDeletedAt()).isEqualTo("2024-01-03T00:00:00");
    }

    @Test
    void findByTrashed_mapsPageAndData() {
        when(service.findByTrashed(any(FindAllCashiers.class)))
                .thenReturn(ApiResponsePagination.<List<CashierResponseDeleteAt>>builder()
                        .status("success").message("ok")
                        .data(List.of(cashierDeleteAt())).pagination(meta()).build());

        TestStreamObserver<ApiResponsePaginationCashierDeleteAt> observer = new TestStreamObserver<>();
        handler.findByTrashed(FindAllCashierRequest.newBuilder().setPage(1).setPageSize(10).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData(0).getCashierId()).isEqualTo(1);
    }

    @Test
    void findAll_whenServiceThrows_propagatesNotFound() {
        when(service.findAll(any(FindAllCashiers.class)))
                .thenThrow(new ResourceNotFoundException("cashier missing"));

        TestStreamObserver<ApiResponsePaginationCashier> observer = new TestStreamObserver<>();
        handler.findAll(FindAllCashierRequest.getDefaultInstance(), observer);

        assertThat(observer.value()).isNull();
        assertThat(observer.isCompleted()).isFalse();
        assertThat(observer.error()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException ex = (StatusRuntimeException) observer.error();
        assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
        assertThat(ex.getStatus().getDescription()).isEqualTo("cashier missing");
    }
}
