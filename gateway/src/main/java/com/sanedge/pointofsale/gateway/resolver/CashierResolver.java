package com.sanedge.pointofsale.gateway.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.gateway.dto.inputs.CashierMerchantMonthInput;
import com.sanedge.pointofsale.gateway.dto.inputs.CashierMerchantMonthTotalInput;
import com.sanedge.pointofsale.gateway.dto.inputs.CashierMonthInput;
import com.sanedge.pointofsale.gateway.dto.inputs.CashierMonthTotalInput;
import com.sanedge.pointofsale.gateway.dto.inputs.CreateCashierInput;
import com.sanedge.pointofsale.gateway.dto.inputs.FindCashierMerchantInput;
import com.sanedge.pointofsale.gateway.dto.inputs.MonthInput;
import com.sanedge.pointofsale.gateway.dto.inputs.PageInput;
import com.sanedge.pointofsale.gateway.dto.inputs.UpdateCashierInput;
import com.sanedge.pointofsale.gateway.dto.inputs.YearInput;
import com.sanedge.pointofsale.gateway.dto.responses.CashierResponse;
import com.sanedge.pointofsale.gateway.dto.responses.CashierResponseDeleteAt;
import com.sanedge.pointofsale.gateway.dto.responses.CashierResponseMonthSales;
import com.sanedge.pointofsale.gateway.dto.responses.CashierResponseMonthTotalSales;
import com.sanedge.pointofsale.gateway.dto.responses.CashierResponseYearSales;
import com.sanedge.pointofsale.gateway.dto.responses.CashierResponseYearTotalSales;
import com.sanedge.pointofsale.gateway.mapper.ProtoMapper;
import com.sanedge.pointofsale.pb.cashier.CashierCommandServiceGrpc;
import com.sanedge.pointofsale.pb.cashier.CashierQueryServiceGrpc;
import com.sanedge.pointofsale.pb.cashier.stats.CashierSalesServiceGrpc;
import com.sanedge.pointofsale.pb.cashier.stats.CashierTotalSalesServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CashierResolver {
    private final CashierQueryServiceGrpc.CashierQueryServiceBlockingStub cashierQueryStub;
    private final CashierCommandServiceGrpc.CashierCommandServiceBlockingStub cashierCommandStub;
    private final CashierSalesServiceGrpc.CashierSalesServiceBlockingStub cashierSalesStub;
    private final CashierTotalSalesServiceGrpc.CashierTotalSalesServiceBlockingStub cashierTotalSalesStub;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<CashierResponse>> cashiers(@Argument PageInput input) {
        var resp = cashierQueryStub.findAll(com.sanedge.pointofsale.pb.cashier.FindAllCashierRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<CashierResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cashiers(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<CashierResponse> cashier(@Argument Integer id) {
        var resp = cashierQueryStub.findById(com.sanedge.pointofsale.pb.cashier.FindByIdCashierRequest.newBuilder()
                .setCashierId(id)
                .build());
        return ApiResponse.<CashierResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cashier(resp.getData()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<CashierResponseDeleteAt>> cashiersActive(@Argument PageInput input) {
        var resp = cashierQueryStub.findByActive(com.sanedge.pointofsale.pb.cashier.FindAllCashierRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<CashierResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cashiersDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<CashierResponseDeleteAt>> cashiersTrashed(@Argument PageInput input) {
        var resp = cashierQueryStub.findByTrashed(com.sanedge.pointofsale.pb.cashier.FindAllCashierRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<CashierResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cashiersDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<CashierResponse>> cashiersByMerchant(@Argument FindCashierMerchantInput input) {
        var resp = cashierQueryStub.findByMerchant(
                com.sanedge.pointofsale.pb.cashier.FindByMerchantCashierRequest.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setPage(nvl(input != null ? input.page() : null))
                        .setPageSize(nvl(input != null ? input.pageSize() : null))
                        .setSearch(nvl(input != null ? input.search() : null))
                        .build());
        return ApiResponsePagination.<List<CashierResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cashiers(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<CashierResponseMonthSales>> cashierMonthSales(@Argument YearInput input) {
        var resp = cashierSalesStub.findMonthSales(com.sanedge.pointofsale.pb.cashier.FindYearCashier.newBuilder()
                .setYear(nvl(input != null ? input.year() : null))
                .build());
        return ApiResponse.<List<CashierResponseMonthSales>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::cashierMonthSales).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<CashierResponseYearSales>> cashierYearSales(@Argument YearInput input) {
        var resp = cashierSalesStub.findYearSales(com.sanedge.pointofsale.pb.cashier.FindYearCashier.newBuilder()
                .setYear(nvl(input != null ? input.year() : null))
                .build());
        return ApiResponse.<List<CashierResponseYearSales>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::cashierYearSales).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<CashierResponseMonthTotalSales>> cashierMonthTotalSales(@Argument MonthInput input) {
        var resp = cashierTotalSalesStub.findMonthlyTotalSales(
                com.sanedge.pointofsale.pb.cashier.FindYearMonthTotalSales.newBuilder()
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<CashierResponseMonthTotalSales>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::cashierMonthTotalSales).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<CashierResponseYearTotalSales>> cashierYearTotalSales(@Argument YearInput input) {
        var resp = cashierTotalSalesStub.findYearlyTotalSales(
                com.sanedge.pointofsale.pb.cashier.FindYearTotalSales.newBuilder()
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<CashierResponseYearTotalSales>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::cashierYearTotalSales).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<CashierResponseMonthSales>> cashierMyMonthSales(@Argument CashierMonthInput input) {
        var resp = cashierSalesStub.findMonthSalesById(
                com.sanedge.pointofsale.pb.cashier.FindYearCashierById.newBuilder()
                        .setCashierId(nvl(input != null ? input.cashierId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<CashierResponseMonthSales>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::cashierMonthSales).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<CashierResponseYearSales>> cashierMyYearSales(@Argument CashierMonthInput input) {
        var resp = cashierSalesStub.findYearSalesById(
                com.sanedge.pointofsale.pb.cashier.FindYearCashierById.newBuilder()
                        .setCashierId(nvl(input != null ? input.cashierId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<CashierResponseYearSales>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::cashierYearSales).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<CashierResponseMonthTotalSales>> cashierMyMonthTotalSales(
            @Argument CashierMonthTotalInput input) {
        var resp = cashierTotalSalesStub.findMonthlyTotalSalesById(
                com.sanedge.pointofsale.pb.cashier.FindYearMonthTotalSalesById.newBuilder()
                        .setCashierId(nvl(input != null ? input.cashierId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<CashierResponseMonthTotalSales>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::cashierMonthTotalSales).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<CashierResponseYearTotalSales>> cashierMyYearTotalSales(
            @Argument CashierMonthInput input) {
        var resp = cashierTotalSalesStub.findYearlyTotalSalesById(
                com.sanedge.pointofsale.pb.cashier.FindYearTotalSalesById.newBuilder()
                        .setCashierId(nvl(input != null ? input.cashierId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<CashierResponseYearTotalSales>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::cashierYearTotalSales).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<CashierResponseMonthSales>> cashierMerchantMonthSales(
            @Argument CashierMerchantMonthInput input) {
        var resp = cashierSalesStub.findMonthSalesByMerchant(
                com.sanedge.pointofsale.pb.cashier.FindYearCashierByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<CashierResponseMonthSales>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::cashierMonthSales).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<CashierResponseYearSales>> cashierMerchantYearSales(
            @Argument CashierMerchantMonthInput input) {
        var resp = cashierSalesStub.findYearSalesByMerchant(
                com.sanedge.pointofsale.pb.cashier.FindYearCashierByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<CashierResponseYearSales>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::cashierYearSales).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<CashierResponseMonthTotalSales>> cashierMerchantMonthTotalSales(
            @Argument CashierMerchantMonthTotalInput input) {
        var resp = cashierTotalSalesStub.findMonthlyTotalSalesByMerchant(
                com.sanedge.pointofsale.pb.cashier.FindYearMonthTotalSalesByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<CashierResponseMonthTotalSales>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::cashierMonthTotalSales).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<CashierResponseYearTotalSales>> cashierMerchantYearTotalSales(
            @Argument CashierMerchantMonthInput input) {
        var resp = cashierTotalSalesStub.findYearlyTotalSalesByMerchant(
                com.sanedge.pointofsale.pb.cashier.FindYearTotalSalesByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<CashierResponseYearTotalSales>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::cashierYearTotalSales).toList())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<CashierResponse> createCashier(@Argument CreateCashierInput input) {
        var resp = cashierCommandStub.createCashier(
                com.sanedge.pointofsale.pb.cashier.CreateCashierRequest.newBuilder()
                        .setMerchantId(nvl(input.merchantId()))
                        .setUserId(nvl(input.userId()))
                        .setName(nvl(input.name()))
                        .build());
        return ApiResponse.<CashierResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cashier(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<CashierResponse> updateCashier(@Argument UpdateCashierInput input) {
        var resp = cashierCommandStub.updateCashier(
                com.sanedge.pointofsale.pb.cashier.UpdateCashierRequest.newBuilder()
                        .setCashierId(nvl(input.cashierId()))
                        .setName(nvl(input.name()))
                        .build());
        return ApiResponse.<CashierResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cashier(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<CashierResponseDeleteAt> trashedCashier(@Argument Integer id) {
        var resp = cashierCommandStub.trashedCashier(
                com.sanedge.pointofsale.pb.cashier.FindByIdCashierRequest.newBuilder()
                        .setCashierId(id)
                        .build());
        return ApiResponse.<CashierResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cashierDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<CashierResponseDeleteAt> restoreCashier(@Argument Integer id) {
        var resp = cashierCommandStub.restoreCashier(
                com.sanedge.pointofsale.pb.cashier.FindByIdCashierRequest.newBuilder()
                        .setCashierId(id)
                        .build());
        return ApiResponse.<CashierResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cashierDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteCashierPermanent(@Argument Integer id) {
        var resp = cashierCommandStub.deleteCashierPermanent(
                com.sanedge.pointofsale.pb.cashier.FindByIdCashierRequest.newBuilder()
                        .setCashierId(id)
                        .build());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> restoreAllCashiers() {
        var resp = cashierCommandStub.restoreAllCashier(Empty.getDefaultInstance());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteAllCashiersPermanent() {
        var resp = cashierCommandStub.deleteAllCashierPermanent(Empty.getDefaultInstance());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    private static int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }
}
