package com.sanedge.pointofsale.cashier.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.cashier.domain.requests.FindAllCashierMerchant;
import com.sanedge.pointofsale.cashier.domain.requests.FindAllCashiers;
import com.sanedge.pointofsale.cashier.service.CashierQueryService;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashier;
import com.sanedge.pointofsale.pb.cashier.ApiResponsePaginationCashier;
import com.sanedge.pointofsale.pb.cashier.ApiResponsePaginationCashierDeleteAt;
import com.sanedge.pointofsale.pb.cashier.CashierQueryServiceGrpc;
import com.sanedge.pointofsale.pb.cashier.CashierResponse;
import com.sanedge.pointofsale.pb.cashier.CashierResponseDeleteAt;
import com.sanedge.pointofsale.pb.cashier.FindAllCashierRequest;
import com.sanedge.pointofsale.pb.cashier.FindByMerchantCashierRequest;
import com.sanedge.pointofsale.pb.cashier.FindByIdCashierRequest;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashierQueryHandler extends CashierQueryServiceGrpc.CashierQueryServiceImplBase {

    private final CashierQueryService cashierQueryService;

    @Override
    public void findAll(FindAllCashierRequest request, StreamObserver<ApiResponsePaginationCashier> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponse>> res = cashierQueryService
                    .findAll(toFindAll(request));

            ApiResponsePaginationCashier.Builder builder = ApiResponsePaginationCashier.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProto(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findById(FindByIdCashierRequest request, StreamObserver<ApiResponseCashier> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.cashier.domain.responses.CashierResponse> res = cashierQueryService
                    .findById(request.getCashierId());

            ApiResponseCashier.Builder builder = ApiResponseCashier.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProto(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByMerchant(FindByMerchantCashierRequest request,
            StreamObserver<ApiResponsePaginationCashier> observer) {
        try {
            FindAllCashierMerchant req = new FindAllCashierMerchant();
            req.setMerchantId(request.getMerchantId());
            req.setSearch(request.getSearch());
            req.setPage(request.getPage());
            req.setPageSize(request.getPageSize());

            ApiResponsePagination<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponse>> res = cashierQueryService
                    .findByMerchant(req);

            ApiResponsePaginationCashier.Builder builder = ApiResponsePaginationCashier.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProto(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByActive(FindAllCashierRequest request,
            StreamObserver<ApiResponsePaginationCashierDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseDeleteAt>> res = cashierQueryService
                    .findByActive(toFindAll(request));

            ApiResponsePaginationCashierDeleteAt.Builder builder = ApiResponsePaginationCashierDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByTrashed(FindAllCashierRequest request,
            StreamObserver<ApiResponsePaginationCashierDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseDeleteAt>> res = cashierQueryService
                    .findByTrashed(toFindAll(request));

            ApiResponsePaginationCashierDeleteAt.Builder builder = ApiResponsePaginationCashierDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private FindAllCashiers toFindAll(FindAllCashierRequest request) {
        FindAllCashiers req = new FindAllCashiers();
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
    }

    private CashierResponse toProto(com.sanedge.pointofsale.cashier.domain.responses.CashierResponse r) {
        return CashierResponse.newBuilder()
                .setCashierId(r.getId() == null ? 0 : r.getId())
                .setMerchantId(r.getMerchantId() == null ? 0 : r.getMerchantId())
                .setName(ProtoMappers.nvl(r.getName()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .build();
    }

    private CashierResponseDeleteAt toProtoDeleteAt(
            com.sanedge.pointofsale.cashier.domain.responses.CashierResponseDeleteAt r) {
        return CashierResponseDeleteAt.newBuilder()
                .setCashierId(r.getId() == null ? 0 : r.getId())
                .setMerchantId(r.getMerchantId() == null ? 0 : r.getMerchantId())
                .setName(ProtoMappers.nvl(r.getName()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .setDeletedAt(ProtoMappers.nvl(r.getDeletedAt()))
                .build();
    }
}
