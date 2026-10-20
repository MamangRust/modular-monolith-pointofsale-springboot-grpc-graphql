package com.sanedge.pointofsale.transaction.service.impl.stats;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.transaction.domain.requests.MonthAmountTransactionRequest;
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
import com.sanedge.pointofsale.transaction.repository.stats.TransactionAmountStatusRepository;
import com.sanedge.pointofsale.transaction.service.stats.TransactionAmountService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionAmountImplService implements TransactionAmountService {

        private final TransactionAmountStatusRepository transactionAmountStatusRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponse<List<TransactionMonthlyAmountSuccessResponse>> findMonthlyAmountSuccess(
                        MonthAmountTransactionRequest req) {
                Attributes attrs = Attributes.builder()
                                .put("year", String.valueOf(req.getYear()))
                                .put("month", String.valueOf(req.getMonth()))
                                .build();

                return tracingMetrics.traceAndMeasure("findMonthlyAmountSuccess", "find_monthly_amount_success", attrs,
                                () -> {
                                        log.info("📊 Fetching monthly SUCCESS transaction amount | year={}, month={}", req.getYear(),
                                                        req.getMonth());

                                        if (req.getYear() == null || req.getMonth() == null) {
                                                log.error("❌ Year or Month is null | req: {}", req);
                                                return ApiResponse.<List<TransactionMonthlyAmountSuccessResponse>>builder()
                                                                .status("error")
                                                                .message("Year and Month must not be null")
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

                                                List<TransactionMonthlyAmountSuccess> rawData = transactionAmountStatusRepository
                                                                .findMonthlyTransactionSuccess(
                                                                                req.getYear(),
                                                                                req.getMonth(),
                                                                                prev.getYear(),
                                                                                prev.getMonthValue());

                                                List<TransactionMonthlyAmountSuccessResponse> response = rawData.stream()
                                                                .map(TransactionMonthlyAmountSuccessResponse::from)
                                                                .collect(Collectors.toList());

                                                log.info("✅ Found {} monthly SUCCESS transaction records", response.size());

                                                return ApiResponse.<List<TransactionMonthlyAmountSuccessResponse>>builder()
                                                                .status("success")
                                                                .message("Monthly success transaction amount retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch monthly SUCCESS transaction amount | year={}, month={}",
                                                                req.getYear(), req.getMonth(), e);
                                                return ApiResponse.<List<TransactionMonthlyAmountSuccessResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to retrieve monthly success transaction data")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<List<TransactionYearlyAmountSuccessResponse>> findYearlyAmountSuccess(Integer year) {
                Attributes attrs = Attributes.builder()
                                .put("year", String.valueOf(year))
                                .build();

                return tracingMetrics.traceAndMeasure("findYearlyAmountSuccess", "find_yearly_amount_success", attrs,
                                () -> {
                                        log.info("📈 Fetching yearly SUCCESS transaction amount | year={}", year);

                                        if (year == null) {
                                                log.error("❌ Year is null");
                                                return ApiResponse.<List<TransactionYearlyAmountSuccessResponse>>builder()
                                                                .status("error")
                                                                .message("Year must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                List<TransactionYearlyAmountSuccess> rawData = transactionAmountStatusRepository
                                                                .findYearlyTransactionSuccess(year);

                                                List<TransactionYearlyAmountSuccessResponse> response = rawData.stream()
                                                                .map(TransactionYearlyAmountSuccessResponse::from)
                                                                .collect(Collectors.toList());

                                                log.info("✅ Found {} yearly SUCCESS transaction records", response.size());

                                                return ApiResponse.<List<TransactionYearlyAmountSuccessResponse>>builder()
                                                                .status("success")
                                                                .message("Yearly success transaction amount retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch yearly SUCCESS transaction amount | year={}", year, e);
                                                return ApiResponse.<List<TransactionYearlyAmountSuccessResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to retrieve yearly success transaction data")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<List<TransactionMonthlyAmountFailedResponse>> findMonthlyAmountFailed(
                        MonthAmountTransactionRequest req) {
                Attributes attrs = Attributes.builder()
                                .put("year", String.valueOf(req.getYear()))
                                .put("month", String.valueOf(req.getMonth()))
                                .build();

                return tracingMetrics.traceAndMeasure("findMonthlyAmountFailed", "find_monthly_amount_failed", attrs,
                                () -> {
                                        log.info("📊 Fetching monthly FAILED transaction amount | year={}, month={}", req.getYear(),
                                                        req.getMonth());

                                        if (req.getYear() == null || req.getMonth() == null) {
                                                log.error("❌ Year or Month is null | req: {}", req);
                                                return ApiResponse.<List<TransactionMonthlyAmountFailedResponse>>builder()
                                                                .status("error")
                                                                .message("Year and Month must not be null")
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

                                                List<TransactionMonthlyAmountFailed> rawData = transactionAmountStatusRepository
                                                                .findMonthlyTransactionFailed(
                                                                                req.getYear(),
                                                                                req.getMonth(),
                                                                                prev.getYear(),
                                                                                prev.getMonthValue());

                                                List<TransactionMonthlyAmountFailedResponse> response = rawData.stream()
                                                                .map(TransactionMonthlyAmountFailedResponse::from)
                                                                .collect(Collectors.toList());

                                                log.info("✅ Found {} monthly FAILED transaction records", response.size());

                                                return ApiResponse.<List<TransactionMonthlyAmountFailedResponse>>builder()
                                                                .status("success")
                                                                .message("Monthly failed transaction amount retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch monthly FAILED transaction amount | year={}, month={}",
                                                                req.getYear(), req.getMonth(), e);
                                                return ApiResponse.<List<TransactionMonthlyAmountFailedResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to retrieve monthly failed transaction data")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<List<TransactionYearlyAmountFailedResponse>> findYearlyAmountFailed(Integer year) {
                Attributes attrs = Attributes.builder()
                                .put("year", String.valueOf(year))
                                .build();

                return tracingMetrics.traceAndMeasure("findYearlyAmountFailed", "find_yearly_amount_failed", attrs,
                                () -> {
                                        log.info("📈 Fetching yearly FAILED transaction amount | year={}", year);

                                        if (year == null) {
                                                log.error("❌ Year is null");
                                                return ApiResponse.<List<TransactionYearlyAmountFailedResponse>>builder()
                                                                .status("error")
                                                                .message("Year must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                List<TransactionYearlyAmountFailed> rawData = transactionAmountStatusRepository
                                                                .findYearlyTransactionFailed(year);

                                                List<TransactionYearlyAmountFailedResponse> response = rawData.stream()
                                                                .map(TransactionYearlyAmountFailedResponse::from)
                                                                .collect(Collectors.toList());

                                                log.info("✅ Found {} yearly FAILED transaction records", response.size());

                                                return ApiResponse.<List<TransactionYearlyAmountFailedResponse>>builder()
                                                                .status("success")
                                                                .message("Yearly failed transaction amount retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch yearly FAILED transaction amount | year={}", year, e);
                                                return ApiResponse.<List<TransactionYearlyAmountFailedResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to retrieve yearly failed transaction data")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }
}
