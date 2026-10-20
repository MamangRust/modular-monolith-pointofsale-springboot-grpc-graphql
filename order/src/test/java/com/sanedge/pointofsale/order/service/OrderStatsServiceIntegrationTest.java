package com.sanedge.pointofsale.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.sanedge.pointofsale.BaseIntegrationTest;
import com.sanedge.pointofsale.order.domain.requests.MonthOrderMerchantRequest;
import com.sanedge.pointofsale.order.domain.requests.MonthTotalRevenue;
import com.sanedge.pointofsale.order.domain.requests.MonthTotalRevenueMerchantRequest;
import com.sanedge.pointofsale.order.domain.requests.YearOrderMerchantRequest;
import com.sanedge.pointofsale.order.domain.requests.YearTotalRevenueMerchantRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderMonthlyResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderMonthlyTotalRevenueResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderYearlyResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderYearlyTotalRevenueResponse;
import com.sanedge.pointofsale.order.repository.stats.OrderSoldOutRepository;
import com.sanedge.pointofsale.order.repository.stats.OrderTotalRevenueRepository;
import com.sanedge.pointofsale.order.repository.statsbymerchant.OrderSoldOutByMerchantRepository;
import com.sanedge.pointofsale.order.repository.statsbymerchant.OrderTotalRevenueByMerchantRepository;
import com.sanedge.pointofsale.order.service.stats.OrderSoldoutService;
import com.sanedge.pointofsale.order.service.stats.OrderTotalRevenueService;
import com.sanedge.pointofsale.order.service.statsbymerchant.OrderSoldOutByMerchantService;
import com.sanedge.pointofsale.order.service.statsbymerchant.OrderTotalRevenueByMerchantService;

public class OrderStatsServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderSoldoutService orderSoldoutService;

    @Autowired
    private OrderTotalRevenueService orderTotalRevenueService;

    @Autowired
    private OrderSoldOutByMerchantService orderSoldOutByMerchantService;

    @Autowired
    private OrderTotalRevenueByMerchantService orderTotalRevenueByMerchantService;

    @MockitoBean
    private OrderSoldOutRepository orderSoldOutRepository;

    @MockitoBean
    private OrderTotalRevenueRepository orderTotalRevenueRepository;

    @MockitoBean
    private OrderSoldOutByMerchantRepository orderSoldOutByMerchantRepository;

    @MockitoBean
    private OrderTotalRevenueByMerchantRepository orderTotalRevenueByMerchantRepository;

    @BeforeEach
    void setupMocks() {
        when(orderSoldOutRepository.findMonthlyOrdersByYear(any())).thenReturn(List.of());
        when(orderSoldOutRepository.findYearlyOrders(any())).thenReturn(List.of());

        when(orderTotalRevenueRepository.findMonthlyTotalRevenue(any(), any(), any(), any())).thenReturn(List.of());
        when(orderTotalRevenueRepository.findYearlyTotalRevenue(any())).thenReturn(List.of());

        when(orderSoldOutByMerchantRepository.findMonthlyOrdersByMerchant(any(), any())).thenReturn(List.of());
        when(orderSoldOutByMerchantRepository.findYearlyOrdersByMerchant(any(), any())).thenReturn(List.of());

        when(orderTotalRevenueByMerchantRepository.findMonthlyTotalRevenueByMerchant(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(orderTotalRevenueByMerchantRepository.findYearlyTotalRevenueByMerchant(any(), any())).thenReturn(List.of());
    }

    @Test
    void testAllOrderStatsServices() {
        ApiResponse<List<OrderMonthlyResponse>> mOrdersResp = orderSoldoutService.findMonthlyOrders(202605);
        assertThat(mOrdersResp).isNotNull();
        assertThat(mOrdersResp.getData()).isNotNull();

        ApiResponse<List<OrderYearlyResponse>> yOrdersResp = orderSoldoutService.findYearlyOrders(2026);
        assertThat(yOrdersResp).isNotNull();
        assertThat(yOrdersResp.getData()).isNotNull();

        MonthTotalRevenue mtrReq = new MonthTotalRevenue();
        mtrReq.setMonth(5);
        mtrReq.setYear(2026);
        ApiResponse<List<OrderMonthlyTotalRevenueResponse>> mStatsResp = orderTotalRevenueService.findMonthlyStats(mtrReq);
        assertThat(mStatsResp).isNotNull();
        assertThat(mStatsResp.getData()).isNotNull();

        ApiResponse<List<OrderYearlyTotalRevenueResponse>> yStatsResp = orderTotalRevenueService.findYearlyStats(2026);
        assertThat(yStatsResp).isNotNull();
        assertThat(yStatsResp.getData()).isNotNull();

        MonthOrderMerchantRequest momReq = new MonthOrderMerchantRequest();
        momReq.setMerchantId(1);
        momReq.setYear(2026);
        ApiResponse<List<OrderMonthlyResponse>> mOrdersMerResp = orderSoldOutByMerchantService.findMonthlyOrdersByMerchant(momReq);
        assertThat(mOrdersMerResp).isNotNull();
        assertThat(mOrdersMerResp.getData()).isNotNull();

        YearOrderMerchantRequest yomReq = new YearOrderMerchantRequest();
        yomReq.setMerchantId(1);
        yomReq.setYear(2026);
        ApiResponse<List<OrderYearlyResponse>> yOrdersMerResp = orderSoldOutByMerchantService.findYearlyOrdersByMerchant(yomReq);
        assertThat(yOrdersMerResp).isNotNull();
        assertThat(yOrdersMerResp.getData()).isNotNull();

        MonthTotalRevenueMerchantRequest mtrmReq = new MonthTotalRevenueMerchantRequest();
        mtrmReq.setMerchantId(1);
        mtrmReq.setYear(2026);
        ApiResponse<List<OrderMonthlyTotalRevenueResponse>> mStatsMerResp = orderTotalRevenueByMerchantService.findMonthlyStatsByMerchant(mtrmReq);
        assertThat(mStatsMerResp).isNotNull();
        assertThat(mStatsMerResp.getData()).isNotNull();

        YearTotalRevenueMerchantRequest ytrmReq = new YearTotalRevenueMerchantRequest();
        ytrmReq.setMerchantId(1);
        ytrmReq.setYear(2026);
        ApiResponse<List<OrderYearlyTotalRevenueResponse>> yStatsMerResp = orderTotalRevenueByMerchantService.findYearlyStatsByMerchant(ytrmReq);
        assertThat(yStatsMerResp).isNotNull();
        assertThat(yStatsMerResp.getData()).isNotNull();
    }
}
