package com.sanedge.pointofsale.transaction.service.impl.statsbymerchant;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.transaction.domain.requests.MonthAmountTransactionMerchant;
import com.sanedge.pointofsale.transaction.domain.requests.YearAmountTransactionMerchant;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyAmountFailedResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyAmountSuccessResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyAmountFailedResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyAmountSuccessResponse;
import com.sanedge.pointofsale.transaction.models.TransactionMonthlyAmountFailed;
import com.sanedge.pointofsale.transaction.models.TransactionMonthlyAmountSuccess;
import com.sanedge.pointofsale.transaction.models.TransactionYearlyAmountFailed;
import com.sanedge.pointofsale.transaction.models.TransactionYearlyAmountSuccess;
import com.sanedge.pointofsale.transaction.repository.statsbymerchant.TransactionAmountByMerchantRepository;
import com.sanedge.pointofsale.transaction.service.statsbymerchant.TransactionAmountByMerchantService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionAmountByMerchantImplService implements TransactionAmountByMerchantService {

        private final TransactionAmountByMerchantRepository transactionAmountByMerchantRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponse<List<TransactionMonthlyAmountSuccessResponse>> findMonthlyAmountSuccessByMerchant(
                        MonthAmountTransactionMerchant req) {
                Attributes attrs = Attributes.builder()
                                .put("merchantId", String.valueOf(req.getMerchantId()))
                                .put("year", String.valueOf(req.getYear()))
                                .put("month", String.valueOf(req.getMonth()))
                                .build();

                return tracingMetrics.traceAndMeasure("findMonthlyAmountSuccessByMerchant",
                                "find_monthly_amount_success_by_merchant", attrs, () -> {
                                        log.info("📊 Fetching monthly SUCCESS transaction amount by merchant | merchantId={}, year={}, month={}",
                                                        req.getMerchantId(), req.getYear(), req.getMonth());

                                        if (req.getMerchantId() == null || req.getYear() == null || req.getMonth() == null) {
                                                log.error("❌ Missing required fields | req: {}", req);
                                                return ApiResponse.<List<TransactionMonthlyAmountSuccessResponse>>builder()
                                                                .status("error")
                                                                .message("Merchant ID, Year, and Month must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        if (req.getMonth() < 1 || req.getMonth() > 12) {
                                                return ApiResponse.<List<TransactionMonthlyAmountSuccessResponse>>builder()
                                                                .status("error")
                                                                .message("Month must be between 1 and 12")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                LocalDate current = LocalDate.of(req.getYear(), req.getMonth(), 1);
                                                LocalDate prev = current.minusMonths(1);

                                                List<TransactionMonthlyAmountSuccess> rawData = transactionAmountByMerchantRepository
                                                                .findMonthlySuccessByMerchant(
                                                                                req.getMerchantId().longValue(),
                                                                                req.getYear(),
                                                                                req.getMonth(),
                                                                                prev.getYear(),
                                                                                prev.getMonthValue());

                                                List<TransactionMonthlyAmountSuccessResponse> response = rawData.stream()
                                                                .map(TransactionMonthlyAmountSuccessResponse::from)
                                                                .collect(Collectors.toList());

                                                log.info("✅ Found {} monthly SUCCESS transaction records for merchant", response.size());

                                                return ApiResponse.<List<TransactionMonthlyAmountSuccessResponse>>builder()
                                                                .status("success")
                                                                .message("Monthly success transaction amount by merchant retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error(
                                                                "💥 Failed to fetch monthly SUCCESS transaction amount by merchant | merchantId={}, year={}, month={}",
                                                                req.getMerchantId(), req.getYear(), req.getMonth(), e);
                                                return ApiResponse.<List<TransactionMonthlyAmountSuccessResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to retrieve monthly success transaction data by merchant")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<List<TransactionYearlyAmountSuccessResponse>> findYearlyAmountSuccessByMerchant(
                        YearAmountTransactionMerchant req) {
                Attributes attrs = Attributes.builder()
                                .put("merchantId", String.valueOf(req.getMerchantId()))
                                .put("year", String.valueOf(req.getYear()))
                                .build();

                return tracingMetrics.traceAndMeasure("findYearlyAmountSuccessByMerchant",
                                "find_yearly_amount_success_by_merchant", attrs, () -> {
                                        log.info("📈 Fetching yearly SUCCESS transaction amount by merchant | merchantId={}, year={}",
                                                        req.getMerchantId(), req.getYear());

                                        if (req.getMerchantId() == null || req.getYear() == null) {
                                                log.error("❌ Missing required fields | req: {}", req);
                                                return ApiResponse.<List<TransactionYearlyAmountSuccessResponse>>builder()
                                                                .status("error")
                                                                .message("Merchant ID and Year must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                List<TransactionYearlyAmountSuccess> rawData = transactionAmountByMerchantRepository
                                                                .findYearlySuccessByMerchant(
                                                                                req.getMerchantId().longValue(),
                                                                                req.getYear());

                                                List<TransactionYearlyAmountSuccessResponse> response = rawData.stream()
                                                                .map(TransactionYearlyAmountSuccessResponse::from)
                                                                .collect(Collectors.toList());

                                                log.info("✅ Found {} yearly SUCCESS transaction records for merchant", response.size());

                                                return ApiResponse.<List<TransactionYearlyAmountSuccessResponse>>builder()
                                                                .status("success")
                                                                .message("Yearly success transaction amount by merchant retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch yearly SUCCESS transaction amount by merchant | merchantId={}, year={}",
                                                                req.getMerchantId(), req.getYear(), e);
                                                return ApiResponse.<List<TransactionYearlyAmountSuccessResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to retrieve yearly success transaction data by merchant")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<List<TransactionMonthlyAmountFailedResponse>> findMonthlyAmountFailedByMerchant(
                        MonthAmountTransactionMerchant req) {
                Attributes attrs = Attributes.builder()
                                .put("merchantId", String.valueOf(req.getMerchantId()))
                                .put("year", String.valueOf(req.getYear()))
                                .put("month", String.valueOf(req.getMonth()))
                                .build();

                return tracingMetrics.traceAndMeasure("findMonthlyAmountFailedByMerchant",
                                "find_monthly_amount_failed_by_merchant", attrs, () -> {
                                        log.info("📊 Fetching monthly FAILED transaction amount by merchant | merchantId={}, year={}, month={}",
                                                        req.getMerchantId(), req.getYear(), req.getMonth());

                                        if (req.getMerchantId() == null || req.getYear() == null || req.getMonth() == null) {
                                                log.error("❌ Missing required fields | req: {}", req);
                                                return ApiResponse.<List<TransactionMonthlyAmountFailedResponse>>builder()
                                                                .status("error")
                                                                .message("Merchant ID, Year, and Month must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        if (req.getMonth() < 1 || req.getMonth() > 12) {
                                                return ApiResponse.<List<TransactionMonthlyAmountFailedResponse>>builder()
                                                                .status("error")
                                                                .message("Month must be between 1 and 12")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                LocalDate current = LocalDate.of(req.getYear(), req.getMonth(), 1);
                                                LocalDate prev = current.minusMonths(1);

                                                List<TransactionMonthlyAmountFailed> rawData = transactionAmountByMerchantRepository
                                                                .findMonthlyFailedByMerchant(
                                                                                req.getMerchantId().longValue(),
                                                                                req.getYear(),
                                                                                req.getMonth(),
                                                                                prev.getYear(),
                                                                                prev.getMonthValue());

                                                List<TransactionMonthlyAmountFailedResponse> response = rawData.stream()
                                                                .map(TransactionMonthlyAmountFailedResponse::from)
                                                                .collect(Collectors.toList());

                                                log.info("✅ Found {} monthly FAILED transaction records for merchant", response.size());

                                                return ApiResponse.<List<TransactionMonthlyAmountFailedResponse>>builder()
                                                                .status("success")
                                                                .message("Monthly failed transaction amount by merchant retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error(
                                                                "💥 Failed to fetch monthly FAILED transaction amount by merchant | merchantId={}, year={}, month={}",
                                                                req.getMerchantId(), req.getYear(), req.getMonth(), e);
                                                return ApiResponse.<List<TransactionMonthlyAmountFailedResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to retrieve monthly failed transaction data by merchant")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<List<TransactionYearlyAmountFailedResponse>> findYearlyAmountFailedByMerchant(
                        YearAmountTransactionMerchant req) {
                Attributes attrs = Attributes.builder()
                                .put("merchantId", String.valueOf(req.getMerchantId()))
                                .put("year", String.valueOf(req.getYear()))
                                .build();

                return tracingMetrics.traceAndMeasure("findYearlyAmountFailedByMerchant",
                                "find_yearly_amount_failed_by_merchant", attrs, () -> {
                                        log.info("📈 Fetching yearly FAILED transaction amount by merchant | merchantId={}, year={}",
                                                        req.getMerchantId(), req.getYear());

                                        if (req.getMerchantId() == null || req.getYear() == null) {
                                                log.error("❌ Missing required fields | req: {}", req);
                                                return ApiResponse.<List<TransactionYearlyAmountFailedResponse>>builder()
                                                                .status("error")
                                                                .message("Merchant ID and Year must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                List<TransactionYearlyAmountFailed> rawData = transactionAmountByMerchantRepository
                                                                .findYearlyFailedByMerchant(
                                                                                req.getMerchantId().longValue(),
                                                                                req.getYear());

                                                List<TransactionYearlyAmountFailedResponse> response = rawData.stream()
                                                                .map(TransactionYearlyAmountFailedResponse::from)
                                                                .collect(Collectors.toList());

                                                log.info("✅ Found {} yearly FAILED transaction records for merchant", response.size());

                                                return ApiResponse.<List<TransactionYearlyAmountFailedResponse>>builder()
                                                                .status("success")
                                                                .message("Yearly failed transaction amount by merchant retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch yearly FAILED transaction amount by merchant | merchantId={}, year={}",
                                                                req.getMerchantId(), req.getYear(), e);
                                                return ApiResponse.<List<TransactionYearlyAmountFailedResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to retrieve yearly failed transaction data by merchant")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }
}
