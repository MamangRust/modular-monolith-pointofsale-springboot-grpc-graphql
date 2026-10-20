package com.sanedge.pointofsale.merchant.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanedge.pointofsale.common.adapter.model.User;
import com.sanedge.pointofsale.common.adapter.user.UserQueryPort;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.common.outbox.Outbox;
import com.sanedge.pointofsale.common.outbox.OutboxEventFactory;
import com.sanedge.pointofsale.common.outbox.OutboxRepository;
import com.sanedge.pointofsale.merchant.domain.requests.CreateMerchantDocumentRequest;
import com.sanedge.pointofsale.merchant.domain.requests.UpdateMerchantDocumentRequest;
import com.sanedge.pointofsale.merchant.domain.requests.UpdateMerchantDocumentStatus;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantDocumentResponse;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantDocumentResponseDeleteAt;
import com.sanedge.pointofsale.merchant.models.Merchant;
import com.sanedge.pointofsale.merchant.models.MerchantDocument;
import com.sanedge.pointofsale.merchant.repository.MerchantDocumentCommandRepository;
import com.sanedge.pointofsale.merchant.repository.MerchantDocumentQueryRepository;
import com.sanedge.pointofsale.merchant.repository.MerchantQueryRepository;
import com.sanedge.pointofsale.merchant.service.MerchantDocumentCommandService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Command side of merchant documents.
 *
 * <p>Port of the blueprint's {@code MerchantDocumentCommandServiceImpl} onto the
 * blocking JPA/Spring style. Two of the four merchant email topics are produced
 * here (document created / document status updated); the blueprint produced
 * neither, so both payloads are new and follow the shared envelope contract.
 * Events are written to the shared {@code outbox} table (V9) via
 * {@link OutboxRepository}.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MerchantDocumentCommandImplService implements MerchantDocumentCommandService {

    private static final String TOPIC_DOCUMENT_CREATE = "email-service-topic-merchant-document-create";
    private static final String TOPIC_DOCUMENT_UPDATE_STATUS = "email-service-topic-merchant-document-update-status";

    private final MerchantQueryRepository merchantQueryRepository;
    private final MerchantDocumentQueryRepository merchantDocumentQueryRepository;
    private final MerchantDocumentCommandRepository merchantDocumentCommandRepository;
    private final OutboxRepository outboxRepository;
    private final UserQueryPort userQueryPort;
    private final TracingMetrics tracingMetrics;

    @Override
    @Transactional
    public ApiResponse<MerchantDocumentResponse> create(CreateMerchantDocumentRequest req) {
        Attributes attrs = Attributes.builder()
                .put("merchant.id", String.valueOf(req.getMerchantId()))
                .build();

        return tracingMetrics.traceAndMeasure("createMerchantDocument", "create", attrs, () -> {
            Merchant merchant = merchantQueryRepository.findMerchantById(req.getMerchantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

            MerchantDocument doc = new MerchantDocument();
            doc.setMerchantId(req.getMerchantId().intValue());
            doc.setDocumentType(req.getDocumentType());
            doc.setDocumentUrl(req.getDocumentUrl());
            doc.setStatus("PENDING");

            MerchantDocument saved = merchantDocumentCommandRepository.save(doc);
            log.info("✅ Merchant document created | id={}", saved.getDocumentId());

            sendDocumentEmail(merchant, TOPIC_DOCUMENT_CREATE,
                    "Merchant Document Submitted",
                    String.format(
                            "Hello,\n\nA new %s document has been submitted for merchant '%s' and is awaiting review.\n\nRegards,\nSupport Team",
                            saved.getDocumentType(), merchant.getName()));

            return ApiResponse.<MerchantDocumentResponse>builder()
                    .status("success")
                    .message("Merchant document created successfully")
                    .data(MerchantDocumentResponse.from(saved))
                    .build();
        });
    }

    @Override
    @Transactional
    public ApiResponse<MerchantDocumentResponse> update(UpdateMerchantDocumentRequest req) {
        Attributes attrs = Attributes.builder()
                .put("doc.id", String.valueOf(req.getDocumentId()))
                .build();

        return tracingMetrics.traceAndMeasure("updateMerchantDocument", "update", attrs, () -> {
            MerchantDocument doc = merchantDocumentQueryRepository.findDocumentById(req.getDocumentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Merchant document not found"));

            if (merchantQueryRepository.findMerchantById(req.getMerchantId()).isEmpty()) {
                throw new ResourceNotFoundException("Merchant not found");
            }

            doc.setMerchantId(req.getMerchantId().intValue());
            doc.setDocumentType(req.getDocumentType());
            doc.setDocumentUrl(req.getDocumentUrl());
            doc.setNote(req.getNote());
            doc.setStatus(req.getStatus());

            MerchantDocument saved = merchantDocumentCommandRepository.save(doc);
            log.info("✅ Merchant document updated | id={}", saved.getDocumentId());

            return ApiResponse.<MerchantDocumentResponse>builder()
                    .status("success")
                    .message("Merchant document updated successfully")
                    .data(MerchantDocumentResponse.from(saved))
                    .build();
        });
    }

    @Override
    @Transactional
    public ApiResponse<MerchantDocumentResponse> updateStatus(UpdateMerchantDocumentStatus req) {
        Attributes attrs = Attributes.builder()
                .put("doc.id", String.valueOf(req.getDocumentId()))
                .build();

        return tracingMetrics.traceAndMeasure("updateMerchantDocumentStatus", "update_status", attrs, () -> {
            MerchantDocument doc = merchantDocumentQueryRepository.findDocumentById(req.getDocumentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Merchant document not found"));

            Merchant merchant = merchantQueryRepository.findMerchantById(req.getMerchantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

            doc.setStatus(req.getStatus());
            doc.setNote(req.getNote());

            MerchantDocument saved = merchantDocumentCommandRepository.save(doc);
            log.info("✅ Merchant document status updated | id={} status={}", saved.getDocumentId(), saved.getStatus());

            sendDocumentEmail(merchant, TOPIC_DOCUMENT_UPDATE_STATUS,
                    "Merchant Document Status Updated",
                    String.format(
                            "Hello,\n\nYour %s document for merchant '%s' has been reviewed. New status: %s.\n\nRegards,\nSupport Team",
                            saved.getDocumentType(), merchant.getName(), saved.getStatus()));

            return ApiResponse.<MerchantDocumentResponse>builder()
                    .status("success")
                    .message("Merchant document status updated successfully")
                    .data(MerchantDocumentResponse.from(saved))
                    .build();
        });
    }

    @Override
    public ApiResponse<MerchantDocumentResponseDeleteAt> trash(Long documentId) {
        Attributes attrs = Attributes.builder()
                .put("doc.id", String.valueOf(documentId))
                .build();

        return tracingMetrics.traceAndMeasure("trashMerchantDocument", "trash", attrs, () -> {
            MerchantDocument doc = merchantDocumentCommandRepository.trashed(documentId);
            if (doc == null) {
                throw new ResourceNotFoundException("Merchant document not found");
            }
            return ApiResponse.<MerchantDocumentResponseDeleteAt>builder()
                    .status("success")
                    .message("Merchant document trashed successfully")
                    .data(MerchantDocumentResponseDeleteAt.from(doc))
                    .build();
        });
    }

    @Override
    public ApiResponse<MerchantDocumentResponseDeleteAt> restore(Long documentId) {
        Attributes attrs = Attributes.builder()
                .put("doc.id", String.valueOf(documentId))
                .build();

        return tracingMetrics.traceAndMeasure("restoreMerchantDocument", "restore", attrs, () -> {
            MerchantDocument doc = merchantDocumentCommandRepository.restore(documentId);
            if (doc == null) {
                throw new ResourceNotFoundException("Merchant document not found");
            }
            return ApiResponse.<MerchantDocumentResponseDeleteAt>builder()
                    .status("success")
                    .message("Merchant document restored successfully")
                    .data(MerchantDocumentResponseDeleteAt.from(doc))
                    .build();
        });
    }

    @Override
    public ApiResponse<Void> deletePermanent(Long documentId) {
        Attributes attrs = Attributes.builder()
                .put("doc.id", String.valueOf(documentId))
                .build();

        return tracingMetrics.traceAndMeasure("deletePermanentMerchantDocument", "delete_permanent", attrs, () -> {
            MerchantDocument doc = merchantDocumentCommandRepository.deletePermanent(documentId);
            if (doc == null) {
                throw new IllegalArgumentException(
                        "Merchant document not found or must be trashed before permanent deletion");
            }
            return ApiResponse.<Void>builder()
                    .status("success")
                    .message("Merchant document permanently deleted")
                    .build();
        });
    }

    @Override
    public ApiResponse<Void> restoreAll() {
        return tracingMetrics.traceAndMeasure("restoreAllMerchantDocuments", "restore_all", () -> {
            if (!merchantDocumentCommandRepository.restoreAllDeleted()) {
                throw new ResourceNotFoundException("No trashed merchant documents found");
            }
            return ApiResponse.<Void>builder()
                    .status("success")
                    .message("All merchant documents restored successfully")
                    .build();
        });
    }

    @Override
    public ApiResponse<Void> deleteAllPermanent() {
        return tracingMetrics.traceAndMeasure("deleteAllPermanentMerchantDocuments", "delete_all_permanent", () -> {
            if (!merchantDocumentCommandRepository.deleteAllDeleted()) {
                throw new ResourceNotFoundException("No trashed merchant documents found");
            }
            return ApiResponse.<Void>builder()
                    .status("success")
                    .message("All trashed merchant documents permanently deleted")
                    .build();
        });
    }

    /**
     * Enqueues a document email to the merchant owner's address.
     *
     * <p>Best-effort: a failed recipient lookup is logged and skipped so it never
     * aborts the document write.
     */
    private void sendDocumentEmail(Merchant merchant, String topic, String subject, String body) {
        try {
            User owner = userQueryPort.findById(merchant.getUserId().intValue());
            OutboxEventFactory.Envelope envelope = OutboxEventFactory.email(topic, owner.email(), subject, body);

            Outbox event = new Outbox();
            event.setAggregateType("MERCHANT");
            event.setAggregateId(String.valueOf(merchant.getMerchantId()));
            event.setTopic(topic);
            event.setPayload(envelope.payload());
            outboxRepository.enqueue(event);
        } catch (RuntimeException e) {
            log.warn("⚠️ Failed to enqueue document email | topic={} merchantId={}: {}",
                    topic, merchant.getMerchantId(), e.getMessage());
        }
    }
}
