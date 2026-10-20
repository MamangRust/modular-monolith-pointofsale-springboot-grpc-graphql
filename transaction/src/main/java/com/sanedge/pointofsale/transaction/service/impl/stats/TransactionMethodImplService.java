package com.sanedge.pointofsale.transaction.service.impl.stats;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.transaction.domain.requests.MonthMethodTransactionRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyMethodResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyMethodResponse;
import com.sanedge.pointofsale.transaction.models.TransactionMonthlyMethod;
import com.sanedge.pointofsale.transaction.models.TransactionYearMethod;
import com.sanedge.pointofsale.transaction.repository.stats.TransactionMethodRepository;
import com.sanedge.pointofsale.transaction.service.stats.TransactionMethodService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionMethodImplService implements TransactionMethodService {
        private final TransactionMethodRepository transactionMethodRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponse<List<TransactionMonthlyMethodResponse>> findMonthlyMethodSuccess(
                        MonthMethodTransactionRequest req) {
                Attributes attrs = Attributes.builder()
                                .put("year", String.valueOf(req.getYear()))
                                .put("month", String.valueOf(req.getMonth()))
                                .build();

                return tracingMetrics.traceAndMeasure("findMonthlyMethodSuccess", "find_monthly_method_success", attrs,
                                () -> {
                                        log.info("📊 Fetching monthly SUCCESS transaction by method | year={}, month={}", req.getYear(),
                                                        req.getMonth());

                                        if (req.getYear() == null || req.getMonth() == null) {
                                                log.error("❌ Year or Month is null | req: {}", req);
                                                return ApiResponse.<List<TransactionMonthlyMethodResponse>>builder()
                                                                .status("error")
                                                                .message("Year and Month must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        if (req.getMonth() < 1 || req.getMonth() > 12) {
                                                return ApiResponse.<List<TransactionMonthlyMethodResponse>>builder()
                                                                .status("error")
                                                                .message("Month must be between 1 and 12")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                LocalDate current = LocalDate.of(req.getYear(), req.getMonth(), 1);
                                                LocalDate prev = current.minusMonths(1);

                                                List<TransactionMonthlyMethod> rawData = transactionMethodRepository
                                                                .findMonthlyMethodsSuccess(
                                                                                req.getYear(),
                                                                                req.getMonth(),
                                                                                prev.getYear(),
                                                                                prev.getMonthValue());

                                                List<TransactionMonthlyMethodResponse> response = rawData.stream()
                                                                .map(TransactionMonthlyMethodResponse::from)
                                                                .collect(Collectors.toList());

                                                log.info("✅ Found {} monthly SUCCESS transaction records by method", response.size());

                                                return ApiResponse.<List<TransactionMonthlyMethodResponse>>builder()
                                                                .status("success")
                                                                .message("Monthly success transaction by method retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch monthly SUCCESS transaction by method | year={}, month={}",
                                                                req.getYear(), req.getMonth(), e);
                                                return ApiResponse.<List<TransactionMonthlyMethodResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to retrieve monthly success transaction data by method")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<List<TransactionYearlyMethodResponse>> findYearlyMethodSuccess(Integer year) {
                Attributes attrs = Attributes.builder()
                                .put("year", String.valueOf(year))
                                .build();

                return tracingMetrics.traceAndMeasure("findYearlyMethodSuccess", "find_yearly_method_success", attrs,
                                () -> {
                                        log.info("📈 Fetching yearly SUCCESS transaction by method | year={}", year);

                                        if (year == null) {
                                                log.error("❌ Year is null");
                                                return ApiResponse.<List<TransactionYearlyMethodResponse>>builder()
                                                                .status("error")
                                                                .message("Year must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                List<TransactionYearMethod> rawData = transactionMethodRepository
                                                                .findYearlyMethodsSuccess(year);

                                                List<TransactionYearlyMethodResponse> response = rawData.stream()
                                                                .map(TransactionYearlyMethodResponse::from)
                                                                .collect(Collectors.toList());

                                                log.info("✅ Found {} yearly SUCCESS transaction records by method", response.size());

                                                return ApiResponse.<List<TransactionYearlyMethodResponse>>builder()
                                                                .status("success")
                                                                .message("Yearly success transaction by method retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch yearly SUCCESS transaction by method | year={}", year, e);
                                                return ApiResponse.<List<TransactionYearlyMethodResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to retrieve yearly success transaction data by method")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<List<TransactionMonthlyMethodResponse>> findMonthlyMethodFailed(
                        MonthMethodTransactionRequest req) {
                Attributes attrs = Attributes.builder()
                                .put("year", String.valueOf(req.getYear()))
                                .put("month", String.valueOf(req.getMonth()))
                                .build();

                return tracingMetrics.traceAndMeasure("findMonthlyMethodFailed", "find_monthly_method_failed", attrs,
                                () -> {
                                        log.info("📊 Fetching monthly FAILED transaction by method | year={}, month={}", req.getYear(),
                                                        req.getMonth());

                                        if (req.getYear() == null || req.getMonth() == null) {
                                                log.error("❌ Year or Month is null | req: {}", req);
                                                return ApiResponse.<List<TransactionMonthlyMethodResponse>>builder()
                                                                .status("error")
                                                                .message("Year and Month must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        if (req.getMonth() < 1 || req.getMonth() > 12) {
                                                return ApiResponse.<List<TransactionMonthlyMethodResponse>>builder()
                                                                .status("error")
                                                                .message("Month must be between 1 and 12")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                LocalDate current = LocalDate.of(req.getYear(), req.getMonth(), 1);
                                                LocalDate prev = current.minusMonths(1);

                                                List<TransactionMonthlyMethod> rawData = transactionMethodRepository
                                                                .findMonthlyMethodsFailed(
                                                                                req.getYear(),
                                                                                req.getMonth(),
                                                                                prev.getYear(),
                                                                                prev.getMonthValue());

                                                List<TransactionMonthlyMethodResponse> response = rawData.stream()
                                                                .map(TransactionMonthlyMethodResponse::from)
                                                                .collect(Collectors.toList());

                                                log.info("✅ Found {} monthly FAILED transaction records by method", response.size());

                                                return ApiResponse.<List<TransactionMonthlyMethodResponse>>builder()
                                                                .status("success")
                                                                .message("Monthly failed transaction by method retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch monthly FAILED transaction by method | year={}, month={}",
                                                                req.getYear(), req.getMonth(), e);
                                                return ApiResponse.<List<TransactionMonthlyMethodResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to retrieve monthly failed transaction data by method")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<List<TransactionYearlyMethodResponse>> findYearlyMethodFailed(Integer year) {
                Attributes attrs = Attributes.builder()
                                .put("year", String.valueOf(year))
                                .build();

                return tracingMetrics.traceAndMeasure("findYearlyMethodFailed", "find_yearly_method_failed", attrs,
                                () -> {
                                        log.info("📈 Fetching yearly FAILED transaction by method | year={}", year);

                                        if (year == null) {
                                                log.error("❌ Year is null");
                                                return ApiResponse.<List<TransactionYearlyMethodResponse>>builder()
                                                                .status("error")
                                                                .message("Year must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                List<TransactionYearMethod> rawData = transactionMethodRepository
                                                                .findYearlyMethodsFailed(year);

                                                List<TransactionYearlyMethodResponse> response = rawData.stream()
                                                                .map(TransactionYearlyMethodResponse::from)
                                                                .collect(Collectors.toList());

                                                log.info("✅ Found {} yearly FAILED transaction records by method", response.size());

                                                return ApiResponse.<List<TransactionYearlyMethodResponse>>builder()
                                                                .status("success")
                                                                .message("Yearly failed transaction by method retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch yearly FAILED transaction by method | year={}", year, e);
                                                return ApiResponse.<List<TransactionYearlyMethodResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to retrieve yearly failed transaction data by method")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }
}
