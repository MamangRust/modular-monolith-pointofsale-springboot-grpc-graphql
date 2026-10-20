package com.sanedge.pointofsale.cashier.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponse;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseDeleteAt;
import com.sanedge.pointofsale.cashier.service.CashierCommandService;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashier;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashierAll;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashierDeleteAt;
import com.sanedge.pointofsale.pb.cashier.CreateCashierRequest;
import com.sanedge.pointofsale.pb.cashier.FindByIdCashierRequest;
import com.sanedge.pointofsale.pb.cashier.UpdateCashierRequest;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

@ExtendWith(MockitoExtension.class)
class CashierCommandHandlerTest {

    @Mock
    private CashierCommandService service;

    @InjectMocks
    private CashierCommandHandler handler;

    private static CashierResponse cashier() {
        return CashierResponse.builder()
                .id(1).merchantId(2).name("Cashier One")
                .createdAt("2024-01-01T00:00:00").updatedAt("2024-01-02T00:00:00").build();
    }

    private static CashierResponseDeleteAt cashierDeleteAt() {
        return CashierResponseDeleteAt.builder()
                .id(1).merchantId(2).name("Cashier One")
                .createdAt("2024-01-01T00:00:00").updatedAt("2024-01-02T00:00:00")
                .deletedAt("2024-01-03T00:00:00").build();
    }

    @Test
    void createCashier_mapsData() {
        when(service.createCashier(any())).thenReturn(ApiResponse.<CashierResponse>builder()
                .status("success").message("created").data(cashier()).build());

        TestStreamObserver<ApiResponseCashier> observer = new TestStreamObserver<>();
        handler.createCashier(CreateCashierRequest.newBuilder()
                .setMerchantId(2).setUserId(3).setName("Cashier One").build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.isCompleted()).isTrue();
        assertThat(observer.value().getStatus()).isEqualTo("success");
        assertThat(observer.value().getMessage()).isEqualTo("created");
        assertThat(observer.value().getData().getCashierId()).isEqualTo(1);
        assertThat(observer.value().getData().getName()).isEqualTo("Cashier One");
    }

    @Test
    void updateCashier_mapsData() {
        when(service.updateCashier(any())).thenReturn(ApiResponse.<CashierResponse>builder()
                .status("success").message("updated").data(cashier()).build());

        TestStreamObserver<ApiResponseCashier> observer = new TestStreamObserver<>();
        handler.updateCashier(UpdateCashierRequest.newBuilder()
                .setCashierId(1).setName("Cashier One").build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData().getCashierId()).isEqualTo(1);
    }

    @Test
    void trashedCashier_mapsData() {
        when(service.trashedCashier(1)).thenReturn(ApiResponse.<CashierResponseDeleteAt>builder()
                .status("success").message("trashed").data(cashierDeleteAt()).build());

        TestStreamObserver<ApiResponseCashierDeleteAt> observer = new TestStreamObserver<>();
        handler.trashedCashier(FindByIdCashierRequest.newBuilder().setCashierId(1).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData().getDeletedAt()).isEqualTo("2024-01-03T00:00:00");
    }

    @Test
    void restoreCashier_mapsData() {
        when(service.restoreCashier(1)).thenReturn(ApiResponse.<CashierResponseDeleteAt>builder()
                .status("success").message("restored").data(cashierDeleteAt()).build());

        TestStreamObserver<ApiResponseCashierDeleteAt> observer = new TestStreamObserver<>();
        handler.restoreCashier(FindByIdCashierRequest.newBuilder().setCashierId(1).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData().getCashierId()).isEqualTo(1);
    }

    @Test
    void deleteCashierPermanent_mapsSuccess() {
        when(service.deleteCashierPermanent(1)).thenReturn(ApiResponse.<Boolean>builder()
                .status("success").message("deleted").data(true).build());

        TestStreamObserver<ApiResponseCashierAll> observer = new TestStreamObserver<>();
        handler.deleteCashierPermanent(FindByIdCashierRequest.newBuilder().setCashierId(1).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getSuccess()).isTrue();
    }

    @Test
    void restoreAllCashier_mapsSuccess() {
        when(service.restoreAllCashier()).thenReturn(ApiResponse.<Boolean>builder()
                .status("success").message("restored all").data(true).build());

        TestStreamObserver<ApiResponseCashierAll> observer = new TestStreamObserver<>();
        handler.restoreAllCashier(Empty.getDefaultInstance(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getSuccess()).isTrue();
    }

    @Test
    void deleteAllCashierPermanent_mapsSuccess() {
        when(service.deleteAllCashierPermanent()).thenReturn(ApiResponse.<Boolean>builder()
                .status("success").message("deleted all").data(true).build());

        TestStreamObserver<ApiResponseCashierAll> observer = new TestStreamObserver<>();
        handler.deleteAllCashierPermanent(Empty.getDefaultInstance(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getSuccess()).isTrue();
    }

    @Test
    void createCashier_whenServiceThrows_propagatesNotFound() {
        when(service.createCashier(any())).thenThrow(new ResourceNotFoundException("merchant missing"));

        TestStreamObserver<ApiResponseCashier> observer = new TestStreamObserver<>();
        handler.createCashier(CreateCashierRequest.newBuilder().setMerchantId(99).setName("x").build(), observer);

        assertThat(observer.value()).isNull();
        assertThat(observer.error()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException ex = (StatusRuntimeException) observer.error();
        assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
        assertThat(ex.getStatus().getDescription()).isEqualTo("merchant missing");
    }
}
