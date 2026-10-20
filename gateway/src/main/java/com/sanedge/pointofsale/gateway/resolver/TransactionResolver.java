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
import com.sanedge.pointofsale.gateway.dto.inputs.CreateTransactionInput;
import com.sanedge.pointofsale.gateway.dto.inputs.PageInput;
import com.sanedge.pointofsale.gateway.dto.inputs.TransactionMerchantMonthInput;
import com.sanedge.pointofsale.gateway.dto.inputs.TransactionMerchantYearInput;
import com.sanedge.pointofsale.gateway.dto.inputs.TransactionMonthInput;
import com.sanedge.pointofsale.gateway.dto.inputs.TransactionYearInput;
import com.sanedge.pointofsale.gateway.dto.inputs.UpdateTransactionInput;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionMonthlyAmountFailedResponse;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionMonthlyAmountSuccessResponse;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionMonthlyMethodResponse;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionResponse;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionResponseDeleteAt;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionYearlyAmountFailedResponse;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionYearlyAmountSuccessResponse;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionYearlyMethodResponse;
import com.sanedge.pointofsale.gateway.mapper.ProtoMapper;
import com.sanedge.pointofsale.pb.transaction.TransactionCommandServiceGrpc;
import com.sanedge.pointofsale.pb.transaction.TransactionQueryServiceGrpc;
import com.sanedge.pointofsale.pb.transaction.stats.TransactionStatsAmountServiceGrpc;
import com.sanedge.pointofsale.pb.transaction.stats.TransactionStatsMethodServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TransactionResolver {

    private final TransactionQueryServiceGrpc.TransactionQueryServiceBlockingStub transactionQueryStub;
    private final TransactionCommandServiceGrpc.TransactionCommandServiceBlockingStub transactionCommandStub;
    private final TransactionStatsAmountServiceGrpc.TransactionStatsAmountServiceBlockingStub transactionStatsAmountStub;
    private final TransactionStatsMethodServiceGrpc.TransactionStatsMethodServiceBlockingStub transactionStatsMethodStub;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<TransactionResponse>> transactions(@Argument PageInput input) {
        var resp = transactionQueryStub.findAll(
                com.sanedge.pointofsale.pb.transaction.FindAllTransactionRequest.newBuilder()
                        .setPage(nvl(input != null ? input.page() : null))
                        .setPageSize(nvl(input != null ? input.pageSize() : null))
                        .setSearch(nvl(input != null ? input.search() : null))
                        .build());
        return ApiResponsePagination.<List<TransactionResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactions(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<TransactionResponseDeleteAt>> transactionsActive(@Argument PageInput input) {
        var resp = transactionQueryStub.findByActive(
                com.sanedge.pointofsale.pb.transaction.FindAllTransactionRequest.newBuilder()
                        .setPage(nvl(input != null ? input.page() : null))
                        .setPageSize(nvl(input != null ? input.pageSize() : null))
                        .setSearch(nvl(input != null ? input.search() : null))
                        .build());
        return ApiResponsePagination.<List<TransactionResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<TransactionResponseDeleteAt>> transactionsTrashed(@Argument PageInput input) {
        var resp = transactionQueryStub.findByTrashed(
                com.sanedge.pointofsale.pb.transaction.FindAllTransactionRequest.newBuilder()
                        .setPage(nvl(input != null ? input.page() : null))
                        .setPageSize(nvl(input != null ? input.pageSize() : null))
                        .setSearch(nvl(input != null ? input.search() : null))
                        .build());
        return ApiResponsePagination.<List<TransactionResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<TransactionResponse> transaction(@Argument Integer id) {
        var resp = transactionQueryStub.findById(
                com.sanedge.pointofsale.pb.transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(id)
                        .build());
        return ApiResponse.<TransactionResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transaction(resp.getData()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<TransactionResponse>> transactionsByMerchant(@Argument Integer merchantId,
            @Argument PageInput input) {
        var resp = transactionQueryStub.findByMerchant(
                com.sanedge.pointofsale.pb.transaction.FindByMerchantTransactionRequest.newBuilder()
                        .setMerchantId(nvl(merchantId))
                        .setPage(nvl(input != null ? input.page() : null))
                        .setPageSize(nvl(input != null ? input.pageSize() : null))
                        .setSearch(nvl(input != null ? input.search() : null))
                        .build());
        return ApiResponsePagination.<List<TransactionResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactions(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionMonthlyAmountSuccessResponse>> transactionMonthlySuccess(
            @Argument TransactionMonthInput input) {
        var resp = transactionStatsAmountStub.findMonthlyTransactionSuccess(
                com.sanedge.pointofsale.pb.transaction.FindYearMonthTransaction.newBuilder()
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<TransactionMonthlyAmountSuccessResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnMonthlyAmountSuccess).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionYearlyAmountSuccessResponse>> transactionYearlySuccess(
            @Argument TransactionYearInput input) {
        var resp = transactionStatsAmountStub.findYearlyTransactionSuccess(
                com.sanedge.pointofsale.pb.transaction.FindYearTransaction.newBuilder()
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<TransactionYearlyAmountSuccessResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnYearlyAmountSuccess).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionMonthlyAmountFailedResponse>> transactionMonthlyFailed(
            @Argument TransactionMonthInput input) {
        var resp = transactionStatsAmountStub.findMonthlyTransactionFailed(
                com.sanedge.pointofsale.pb.transaction.FindYearMonthTransaction.newBuilder()
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<TransactionMonthlyAmountFailedResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnMonthlyAmountFailed).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionYearlyAmountFailedResponse>> transactionYearlyFailed(
            @Argument TransactionYearInput input) {
        var resp = transactionStatsAmountStub.findYearlyTransactionFailed(
                com.sanedge.pointofsale.pb.transaction.FindYearTransaction.newBuilder()
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<TransactionYearlyAmountFailedResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnYearlyAmountFailed).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionMonthlyAmountSuccessResponse>> transactionMerchantMonthlySuccess(
            @Argument TransactionMerchantMonthInput input) {
        var resp = transactionStatsAmountStub.findMonthlyTransactionSuccessByMerchant(
                com.sanedge.pointofsale.pb.transaction.FindYearMonthTransactionByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<TransactionMonthlyAmountSuccessResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnMonthlyAmountSuccess).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionYearlyAmountSuccessResponse>> transactionMerchantYearlySuccess(
            @Argument TransactionMerchantYearInput input) {
        var resp = transactionStatsAmountStub.findYearlyTransactionSuccessByMerchant(
                com.sanedge.pointofsale.pb.transaction.FindYearTransactionByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<TransactionYearlyAmountSuccessResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnYearlyAmountSuccess).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionMonthlyAmountFailedResponse>> transactionMerchantMonthlyFailed(
            @Argument TransactionMerchantMonthInput input) {
        var resp = transactionStatsAmountStub.findMonthlyTransactionFailedByMerchant(
                com.sanedge.pointofsale.pb.transaction.FindYearMonthTransactionByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<TransactionMonthlyAmountFailedResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnMonthlyAmountFailed).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionYearlyAmountFailedResponse>> transactionMerchantYearlyFailed(
            @Argument TransactionMerchantYearInput input) {
        var resp = transactionStatsAmountStub.findYearlyTransactionFailedByMerchant(
                com.sanedge.pointofsale.pb.transaction.FindYearTransactionByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<TransactionYearlyAmountFailedResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnYearlyAmountFailed).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionMonthlyMethodResponse>> transactionMonthlyMethodSuccess(
            @Argument TransactionMonthInput input) {
        var resp = transactionStatsMethodStub.findMonthlyMethodSuccess(
                com.sanedge.pointofsale.pb.transaction.FindYearMonthTransaction.newBuilder()
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<TransactionMonthlyMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnMonthlyMethod).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionYearlyMethodResponse>> transactionYearlyMethodSuccess(
            @Argument TransactionYearInput input) {
        var resp = transactionStatsMethodStub.findYearlyMethodSuccess(
                com.sanedge.pointofsale.pb.transaction.FindYearTransaction.newBuilder()
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<TransactionYearlyMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnYearlyMethod).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionMonthlyMethodResponse>> transactionMonthlyMethodFailed(
            @Argument TransactionMonthInput input) {
        var resp = transactionStatsMethodStub.findMonthlyMethodFailed(
                com.sanedge.pointofsale.pb.transaction.FindYearMonthTransaction.newBuilder()
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<TransactionMonthlyMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnMonthlyMethod).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionYearlyMethodResponse>> transactionYearlyMethodFailed(
            @Argument TransactionYearInput input) {
        var resp = transactionStatsMethodStub.findYearlyMethodFailed(
                com.sanedge.pointofsale.pb.transaction.FindYearTransaction.newBuilder()
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<TransactionYearlyMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnYearlyMethod).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionMonthlyMethodResponse>> transactionMerchantMonthlyMethodSuccess(
            @Argument TransactionMerchantMonthInput input) {
        var resp = transactionStatsMethodStub.findMonthlyMethodSuccessByMerchant(
                com.sanedge.pointofsale.pb.transaction.FindYearMonthTransactionByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<TransactionMonthlyMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnMonthlyMethod).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionYearlyMethodResponse>> transactionMerchantYearlyMethodSuccess(
            @Argument TransactionMerchantYearInput input) {
        var resp = transactionStatsMethodStub.findYearlyMethodSuccessByMerchant(
                com.sanedge.pointofsale.pb.transaction.FindYearTransactionByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<TransactionYearlyMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnYearlyMethod).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionMonthlyMethodResponse>> transactionMerchantMonthlyMethodFailed(
            @Argument TransactionMerchantMonthInput input) {
        var resp = transactionStatsMethodStub.findMonthlyMethodFailedByMerchant(
                com.sanedge.pointofsale.pb.transaction.FindYearMonthTransactionByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<TransactionMonthlyMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnMonthlyMethod).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TransactionYearlyMethodResponse>> transactionMerchantYearlyMethodFailed(
            @Argument TransactionMerchantYearInput input) {
        var resp = transactionStatsMethodStub.findYearlyMethodFailedByMerchant(
                com.sanedge.pointofsale.pb.transaction.FindYearTransactionByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<TransactionYearlyMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::txnYearlyMethod).toList())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<TransactionResponse> createTransaction(@Argument CreateTransactionInput input) {
        var resp = transactionCommandStub.createTransaction(
                com.sanedge.pointofsale.pb.transaction.CreateTransactionRequest.newBuilder()
                        .setOrderId(nvl(input.orderID()))
                        .setMerchantId(nvl(input.merchantID()))
                        .setPaymentMethod(nvl(input.paymentMethod()))
                        .setAmount(nvl(input.amount()))
                        .build());
        return ApiResponse.<TransactionResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transaction(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<TransactionResponse> updateTransaction(@Argument UpdateTransactionInput input) {
        var resp = transactionCommandStub.updateTransaction(
                com.sanedge.pointofsale.pb.transaction.UpdateTransactionRequest.newBuilder()
                        .setTransactionId(nvl(input.transactionID()))
                        .setOrderId(nvl(input.orderID()))
                        .setMerchantId(nvl(input.merchantID()))
                        .setPaymentMethod(nvl(input.paymentMethod()))
                        .setAmount(nvl(input.amount()))
                        .setPaymentStatus(nvl(input.paymentStatus()))
                        .build());
        return ApiResponse.<TransactionResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transaction(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<TransactionResponseDeleteAt> trashedTransaction(@Argument Integer id) {
        var resp = transactionCommandStub.trashedTransaction(
                com.sanedge.pointofsale.pb.transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(id)
                        .build());
        return ApiResponse.<TransactionResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<TransactionResponseDeleteAt> restoreTransaction(@Argument Integer id) {
        var resp = transactionCommandStub.restoreTransaction(
                com.sanedge.pointofsale.pb.transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(id)
                        .build());
        return ApiResponse.<TransactionResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteTransactionPermanent(@Argument Integer id) {
        var resp = transactionCommandStub.deleteTransactionPermanent(
                com.sanedge.pointofsale.pb.transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(id)
                        .build());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> restoreAllTransactions() {
        var resp = transactionCommandStub.restoreAllTransaction(Empty.getDefaultInstance());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteAllTransactionsPermanent() {
        var resp = transactionCommandStub.deleteAllTransactionPermanent(Empty.getDefaultInstance());
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
