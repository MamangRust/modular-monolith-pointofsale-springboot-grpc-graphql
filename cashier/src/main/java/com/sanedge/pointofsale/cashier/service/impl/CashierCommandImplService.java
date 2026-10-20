package com.sanedge.pointofsale.cashier.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.cashier.domain.requests.CreateCashierRequest;
import com.sanedge.pointofsale.cashier.domain.requests.UpdateCashierRequest;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponse;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseDeleteAt;
import com.sanedge.pointofsale.cashier.models.Cashier;
import com.sanedge.pointofsale.cashier.repository.CashierCommandRepository;
import com.sanedge.pointofsale.cashier.repository.CashierQueryRepository;
import com.sanedge.pointofsale.cashier.service.CashierCommandService;
import com.sanedge.pointofsale.common.adapter.merchant.MerchantQueryPort;
import com.sanedge.pointofsale.common.adapter.user.UserQueryPort;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.common.observability.TracingMetrics;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CashierCommandImplService implements CashierCommandService {
    private final UserQueryPort userQueryPort;
    private final MerchantQueryPort merchantQueryPort;
    private final CashierCommandRepository cashierCommandRepository;
    private final CashierQueryRepository cashierQueryRepository;
    private final TracingMetrics tracingMetrics;

    @Override
    public ApiResponse<CashierResponse> createCashier(CreateCashierRequest req) {
        Attributes attrs = Attributes.builder()
                .put("cashier.name", req.getName() != null ? req.getName() : "")
                .build();

        return tracingMetrics.traceAndMeasure("createCashier", "create_cashier", attrs, () -> {
            try {
                log.info("🆕 Creating cashier for merchantId={}, userId={}, name={}", req.getMerchantId(), req.getUserId(),
                        req.getName());

                validateMerchantExists(req.getMerchantId());
                validateUserExists(req.getUserId());

                cashierQueryRepository.findByNameAndMerchantId(req.getName(), req.getMerchantId().longValue())
                        .ifPresent(c -> {
                            throw new IllegalArgumentException(
                                    "Cashier with name '" + req.getName() + "' already exists for this merchant");
                        });

                Cashier cashier = new Cashier();
                cashier.setMerchantId(req.getMerchantId().longValue());
                cashier.setUserId(req.getUserId().longValue());
                cashier.setName(req.getName());
                cashier.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                cashier.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

                Cashier saved = cashierCommandRepository.save(cashier);

                log.info("✅ Cashier created successfully with id={}", saved.getCashierId());

                return ApiResponse.<CashierResponse>builder()
                        .status("success")
                        .message("✅ Cashier created successfully!")
                        .data(CashierResponse.from(saved))
                        .build();
            } catch (ResourceNotFoundException e) {
                throw e;
            } catch (Exception e) {
                log.error("💥 Failed to create cashier", e);
                return ApiResponse.<CashierResponse>builder()
                        .status("error")
                        .message(e.getMessage())
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<CashierResponse> updateCashier(UpdateCashierRequest req) {
        Attributes attrs = Attributes.builder()
                .put("cashier.id", req.getCashierId() != null ? req.getCashierId().toString() : "null")
                .build();

        return tracingMetrics.traceAndMeasure("updateCashier", "update_cashier", attrs, () -> {
            try {
                log.info("🔄 Updating cashier id={}", req.getCashierId());

                Cashier cashier = cashierCommandRepository.findById(req.getCashierId().longValue())
                        .orElseThrow(() -> new ResourceNotFoundException("Cashier not found"));

                cashier.setName(req.getName());
                cashier.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

                Cashier updated = cashierCommandRepository.save(cashier);

                log.info("✅ Cashier updated successfully id={}", updated.getCashierId());

                return ApiResponse.<CashierResponse>builder()
                        .status("success")
                        .message("✅ Cashier updated successfully!")
                        .data(CashierResponse.from(updated))
                        .build();
            } catch (ResourceNotFoundException e) {
                throw e;
            } catch (Exception e) {
                log.error("💥 Failed to update cashier id={}", req.getCashierId(), e);
                return ApiResponse.<CashierResponse>builder()
                        .status("error")
                        .message(e.getMessage())
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<CashierResponseDeleteAt> trashedCashier(Integer cashierId) {
        Attributes attrs = Attributes.builder()
                .put("cashier.id", cashierId != null ? cashierId.toString() : "null")
                .build();

        return tracingMetrics.traceAndMeasure("trashCashier", "trash_cashier", attrs, () -> {
            log.info("🗑️ Trashing cashier id={}", cashierId);
            try {
                Cashier cashier = cashierCommandRepository.trashed(cashierId.longValue());
                return ApiResponse.<CashierResponseDeleteAt>builder()
                        .status("success")
                        .message("🗑️ Cashier trashed successfully!")
                        .data(CashierResponseDeleteAt.from(cashier))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to trash cashier id={}", cashierId, e);
                return ApiResponse.<CashierResponseDeleteAt>builder()
                        .status("error")
                        .message("Failed to trash cashier: " + e.getMessage())
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<CashierResponseDeleteAt> restoreCashier(Integer cashierId) {
        Attributes attrs = Attributes.builder()
                .put("cashier.id", cashierId != null ? cashierId.toString() : "null")
                .build();

        return tracingMetrics.traceAndMeasure("restoreCashier", "restore_cashier", attrs, () -> {
            log.info("♻️ Restoring cashier id={}", cashierId);
            try {
                Cashier cashier = cashierCommandRepository.restore(cashierId.longValue());
                return ApiResponse.<CashierResponseDeleteAt>builder()
                        .status("success")
                        .message("♻️ Cashier restored successfully!")
                        .data(CashierResponseDeleteAt.from(cashier))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore cashier id={}", cashierId, e);
                return ApiResponse.<CashierResponseDeleteAt>builder()
                        .status("error")
                        .message("Failed to restore cashier: " + e.getMessage())
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteCashierPermanent(Integer cashierId) {
        Attributes attrs = Attributes.builder()
                .put("cashier.id", cashierId != null ? cashierId.toString() : "null")
                .build();

        return tracingMetrics.traceAndMeasure("deleteCashierPermanent", "delete_cashier_permanent", attrs, () -> {
            log.info("🧨 Permanently deleting cashier id={}", cashierId);
            try {
                cashierCommandRepository.deletePermanent(cashierId.longValue());
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("🧨 Cashier permanently deleted!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to permanently delete cashier id={}", cashierId, e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to permanently delete cashier: " + e.getMessage())
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> restoreAllCashier() {
        return tracingMetrics.traceAndMeasure("restoreAllCashier", "restore_all_cashier", () -> {
            log.info("🔄 Restoring ALL trashed cashiers");
            try {
                cashierCommandRepository.restoreAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("🔄 All cashiers restored successfully!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore all cashiers", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to restore all cashiers: " + e.getMessage())
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteAllCashierPermanent() {
        return tracingMetrics.traceAndMeasure("deleteAllCashierPermanent", "delete_all_cashier_permanent", () -> {
            log.info("💣 Permanently deleting ALL trashed cashiers");
            try {
                cashierCommandRepository.deleteAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("💣 All cashiers permanently deleted!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to delete all cashiers", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to delete all cashiers: " + e.getMessage())
                        .data(false)
                        .build();
            }
        });
    }

    private void validateMerchantExists(Integer merchantId) {
        merchantQueryPort.findByMerchantId(merchantId);
    }

    private void validateUserExists(Integer userId) {
        userQueryPort.findById(userId);
    }
}
