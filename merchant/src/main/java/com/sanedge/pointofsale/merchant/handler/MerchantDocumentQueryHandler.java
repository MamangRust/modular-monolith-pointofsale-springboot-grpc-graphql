package com.sanedge.pointofsale.merchant.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantDocumentResponse;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantDocumentResponseDeleteAt;
import com.sanedge.pointofsale.merchant.service.MerchantDocumentQueryService;
import com.sanedge.pointofsale.pb.merchant_document.ApiResponseMerchantDocument;
import com.sanedge.pointofsale.pb.merchant_document.ApiResponsePaginationMerchantDocument;
import com.sanedge.pointofsale.pb.merchant_document.ApiResponsePaginationMerchantDocumentAt;
import com.sanedge.pointofsale.pb.merchant_document.FindAllMerchantDocumentsRequest;
import com.sanedge.pointofsale.pb.merchant_document.FindMerchantDocumentByIdRequest;
import com.sanedge.pointofsale.pb.merchant_document.MerchantDocument;
import com.sanedge.pointofsale.pb.merchant_document.MerchantDocumentDeleteAt;
import com.sanedge.pointofsale.pb.merchant_document.MerchantDocumentQueryServiceGrpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

/** gRPC query surface for merchant documents (blocking style). */
@Service
@RequiredArgsConstructor
public class MerchantDocumentQueryHandler extends MerchantDocumentQueryServiceGrpc.MerchantDocumentQueryServiceImplBase {

    private final MerchantDocumentQueryService merchantDocumentQueryService;

    @Override
    public void findAll(FindAllMerchantDocumentsRequest request,
            StreamObserver<ApiResponsePaginationMerchantDocument> observer) {
        try {
            ApiResponsePagination<List<MerchantDocumentResponse>> res = merchantDocumentQueryService
                    .findAll(toDomainRequest(request));

            ApiResponsePaginationMerchantDocument.Builder builder = ApiResponsePaginationMerchantDocument.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(d -> builder.addData(MerchantDocumentCommandHandler.toProto(d)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findAllActive(FindAllMerchantDocumentsRequest request,
            StreamObserver<ApiResponsePaginationMerchantDocumentAt> observer) {
        try {
            ApiResponsePagination<List<MerchantDocumentResponseDeleteAt>> res = merchantDocumentQueryService
                    .findAllActive(toDomainRequest(request));

            ApiResponsePaginationMerchantDocumentAt.Builder builder = ApiResponsePaginationMerchantDocumentAt
                    .newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(d -> builder.addData(MerchantDocumentCommandHandler.toProtoDeleteAt(d)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findAllTrashed(FindAllMerchantDocumentsRequest request,
            StreamObserver<ApiResponsePaginationMerchantDocumentAt> observer) {
        try {
            ApiResponsePagination<List<MerchantDocumentResponseDeleteAt>> res = merchantDocumentQueryService
                    .findAllTrashed(toDomainRequest(request));

            ApiResponsePaginationMerchantDocumentAt.Builder builder = ApiResponsePaginationMerchantDocumentAt
                    .newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(d -> builder.addData(MerchantDocumentCommandHandler.toProtoDeleteAt(d)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findById(FindMerchantDocumentByIdRequest request,
            StreamObserver<ApiResponseMerchantDocument> observer) {
        try {
            ApiResponse<MerchantDocumentResponse> res = merchantDocumentQueryService
                    .findById((long) request.getDocumentId());

            observer.onNext(ApiResponseMerchantDocument.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setData(MerchantDocumentCommandHandler.toProto(res.getData()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private static com.sanedge.pointofsale.merchant.domain.requests.FindAllMerchantDocuments toDomainRequest(
            FindAllMerchantDocumentsRequest request) {
        com.sanedge.pointofsale.merchant.domain.requests.FindAllMerchantDocuments req = new com.sanedge.pointofsale.merchant.domain.requests.FindAllMerchantDocuments();
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
    }

    // Kept for symmetry with the command handler's proto mapper naming.
    @SuppressWarnings("unused")
    private static MerchantDocument toProto(MerchantDocumentResponse r) {
        return MerchantDocumentCommandHandler.toProto(r);
    }

    @SuppressWarnings("unused")
    private static MerchantDocumentDeleteAt toProtoDeleteAt(MerchantDocumentResponseDeleteAt r) {
        return MerchantDocumentCommandHandler.toProtoDeleteAt(r);
    }
}
