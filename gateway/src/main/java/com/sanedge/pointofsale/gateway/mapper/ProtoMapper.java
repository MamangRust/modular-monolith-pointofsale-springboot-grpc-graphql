package com.sanedge.pointofsale.gateway.mapper;

import java.util.List;

import com.sanedge.pointofsale.gateway.dto.responses.CashierResponse;
import com.sanedge.pointofsale.gateway.dto.responses.CashierResponseDeleteAt;
import com.sanedge.pointofsale.gateway.dto.responses.CashierResponseMonthSales;
import com.sanedge.pointofsale.gateway.dto.responses.CashierResponseMonthTotalSales;
import com.sanedge.pointofsale.gateway.dto.responses.CashierResponseYearSales;
import com.sanedge.pointofsale.gateway.dto.responses.CashierResponseYearTotalSales;
import com.sanedge.pointofsale.gateway.dto.responses.CategoriesMonthPriceResponse;
import com.sanedge.pointofsale.gateway.dto.responses.CategoriesMonthlyTotalPriceResponse;
import com.sanedge.pointofsale.gateway.dto.responses.CategoriesYearPriceResponse;
import com.sanedge.pointofsale.gateway.dto.responses.CategoriesYearlyTotalPriceResponse;
import com.sanedge.pointofsale.gateway.dto.responses.CategoryResponse;
import com.sanedge.pointofsale.gateway.dto.responses.CategoryResponseDeleteAt;
import com.sanedge.pointofsale.gateway.dto.responses.MerchantResponse;
import com.sanedge.pointofsale.gateway.dto.responses.MerchantResponseDeleteAt;
import com.sanedge.pointofsale.gateway.dto.responses.OrderMonthlyResponse;
import com.sanedge.pointofsale.gateway.dto.responses.OrderMonthlyTotalRevenueResponse;
import com.sanedge.pointofsale.gateway.dto.responses.OrderResponse;
import com.sanedge.pointofsale.gateway.dto.responses.OrderResponseDeleteAt;
import com.sanedge.pointofsale.gateway.dto.responses.OrderYearlyResponse;
import com.sanedge.pointofsale.gateway.dto.responses.OrderYearlyTotalRevenueResponse;
import com.sanedge.pointofsale.gateway.dto.responses.ProductResponse;
import com.sanedge.pointofsale.gateway.dto.responses.ProductResponseDeleteAt;
import com.sanedge.pointofsale.gateway.dto.responses.RoleResponse;
import com.sanedge.pointofsale.gateway.dto.responses.RoleResponseDeleteAt;
import com.sanedge.pointofsale.gateway.dto.responses.TokenResponse;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionMonthlyAmountFailedResponse;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionMonthlyAmountSuccessResponse;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionMonthlyMethodResponse;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionResponse;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionResponseDeleteAt;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionYearlyAmountFailedResponse;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionYearlyAmountSuccessResponse;
import com.sanedge.pointofsale.gateway.dto.responses.TransactionYearlyMethodResponse;
import com.sanedge.pointofsale.gateway.dto.responses.UserResponse;
import com.sanedge.pointofsale.gateway.dto.responses.UserResponseDeleteAt;

/**
 * Maps generated protobuf response messages onto the gateway-local REST DTOs so
 * that the JSON contract is byte-for-byte identical to the former monolith.
 * Protobuf types are fully qualified to avoid simple-name collisions with the
 * gateway DTOs (e.g. {@code RoleResponse}).
 */
public final class ProtoMapper {

    private ProtoMapper() {
    }

    // ---- auth ----
    public static TokenResponse token(com.sanedge.pointofsale.pb.auth.TokenResponse p) {
        if (p == null) {
            return null;
        }
        return TokenResponse.builder()
                .access_token(p.getAccessToken())
                .refresh_token(p.getRefreshToken())
                .build();
    }

    // ---- role ----
    public static RoleResponse role(com.sanedge.pointofsale.pb.role.RoleResponse p) {
        if (p == null) {
            return null;
        }
        return RoleResponse.builder()
                .id(p.getRoleId())
                .name(p.getName())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static RoleResponseDeleteAt roleDeleteAt(com.sanedge.pointofsale.pb.role.RoleResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return RoleResponseDeleteAt.builder()
                .id(p.getRoleId())
                .name(p.getName())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(p.getDeletedAt())
                .build();
    }

    public static List<RoleResponse> roles(List<com.sanedge.pointofsale.pb.role.RoleResponse> list) {
        return list.stream().map(ProtoMapper::role).toList();
    }

    public static List<RoleResponseDeleteAt> rolesDeleteAt(
            List<com.sanedge.pointofsale.pb.role.RoleResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::roleDeleteAt).toList();
    }

    // ---- user ----
    public static UserResponse user(com.sanedge.pointofsale.pb.user.UserResponse p) {
        if (p == null) {
            return null;
        }
        return UserResponse.builder()
                .id(p.getId())
                .username(p.getUsername())
                .firstname(p.getFirstname())
                .lastname(p.getLastname())
                .email(p.getEmail())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static UserResponseDeleteAt userDeleteAt(com.sanedge.pointofsale.pb.user.UserResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return UserResponseDeleteAt.builder()
                .id(p.getId())
                .firstname(p.getFirstname())
                .lastname(p.getLastname())
                .email(p.getEmail())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(p.getDeletedAt())
                .build();
    }

    public static List<UserResponse> users(List<com.sanedge.pointofsale.pb.user.UserResponse> list) {
        return list.stream().map(ProtoMapper::user).toList();
    }

    public static List<UserResponseDeleteAt> usersDeleteAt(
            List<com.sanedge.pointofsale.pb.user.UserResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::userDeleteAt).toList();
    }

    // ---- merchant ----
    public static MerchantResponse merchant(com.sanedge.pointofsale.pb.merchant.MerchantResponse p) {
        if (p == null) {
            return null;
        }
        return MerchantResponse.builder()
                .id((long) p.getMerchantId())
                .userId(p.getUserId())
                .name(p.getName())
                .description(p.getDescription())
                .address(p.getAddress())
                .contactEmail(p.getContactEmail())
                .contactPhone(p.getContactPhone())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static MerchantResponseDeleteAt merchantDeleteAt(
            com.sanedge.pointofsale.pb.merchant.MerchantResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return MerchantResponseDeleteAt.builder()
                .id((long) p.getMerchantId())
                .userId(p.getUserId())
                .name(p.getName())
                .description(p.getDescription())
                .address(p.getAddress())
                .contactEmail(p.getContactEmail())
                .contactPhone(p.getContactPhone())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(p.getDeletedAt())
                .build();
    }

    public static List<MerchantResponse> merchants(List<com.sanedge.pointofsale.pb.merchant.MerchantResponse> list) {
        return list.stream().map(ProtoMapper::merchant).toList();
    }

    public static List<MerchantResponseDeleteAt> merchantsDeleteAt(
            List<com.sanedge.pointofsale.pb.merchant.MerchantResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::merchantDeleteAt).toList();
    }

    // ---- category ----
    public static CategoryResponse category(com.sanedge.pointofsale.pb.category.CategoryResponse p) {
        if (p == null) {
            return null;
        }
        return CategoryResponse.builder()
                .id((long) p.getCategoryId())
                .name(p.getName())
                .description(p.getDescription())
                .slugCategory(p.getSlugCategory())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static CategoryResponseDeleteAt categoryDeleteAt(
            com.sanedge.pointofsale.pb.category.CategoryResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return CategoryResponseDeleteAt.builder()
                .id((long) p.getCategoryId())
                .name(p.getName())
                .description(p.getDescription())
                .slugCategory(p.getSlugCategory())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(p.getDeletedAt())
                .build();
    }

    public static List<CategoryResponse> categories(List<com.sanedge.pointofsale.pb.category.CategoryResponse> list) {
        return list.stream().map(ProtoMapper::category).toList();
    }

    public static List<CategoryResponseDeleteAt> categoriesDeleteAt(
            List<com.sanedge.pointofsale.pb.category.CategoryResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::categoryDeleteAt).toList();
    }

    public static CategoriesMonthPriceResponse categoryMonthPrice(
            com.sanedge.pointofsale.pb.category.CategoryMonthPriceResponse p) {
        return CategoriesMonthPriceResponse.builder()
                .month(p.getMonth())
                .categoryId(p.getCategoryId())
                .categoryName(p.getCategoryName())
                .orderCount(p.getOrderCount())
                .itemsSold(p.getItemsSold())
                .totalRevenue(p.getTotalRevenue())
                .build();
    }

    public static CategoriesYearPriceResponse categoryYearPrice(
            com.sanedge.pointofsale.pb.category.CategoryYearPriceResponse p) {
        return CategoriesYearPriceResponse.builder()
                .year(p.getYear())
                .categoryId(p.getCategoryId())
                .categoryName(p.getCategoryName())
                .orderCount(p.getOrderCount())
                .itemsSold(p.getItemsSold())
                .totalRevenue(p.getTotalRevenue())
                .uniqueProductsSold(p.getUniqueProductsSold())
                .build();
    }

    public static CategoriesMonthlyTotalPriceResponse categoryMonthTotalPrice(
            com.sanedge.pointofsale.pb.category.CategoryMonthTotalPriceResponse p) {
        return CategoriesMonthlyTotalPriceResponse.builder()
                .year(p.getYear())
                .month(p.getMonth())
                .totalRevenue(p.getTotalRevenue())
                .build();
    }

    public static CategoriesYearlyTotalPriceResponse categoryYearTotalPrice(
            com.sanedge.pointofsale.pb.category.CategoryYearTotalPriceResponse p) {
        return CategoriesYearlyTotalPriceResponse.builder()
                .year(p.getYear())
                .totalRevenue(p.getTotalRevenue())
                .build();
    }

    // ---- product ----
    public static ProductResponse product(com.sanedge.pointofsale.pb.product.ProductResponse p) {
        if (p == null) {
            return null;
        }
        return ProductResponse.builder()
                .id((long) p.getProductId())
                .merchantId(p.getMerchantId())
                .categoryId(p.getCategoryId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .countInStock(p.getCountInStock())
                .brand(p.getBrand())
                .weight(parseIntSafe(p.getWeight()))
                .slugProduct(p.getSlugProduct())
                .imageProduct(p.getImageProduct())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static ProductResponseDeleteAt productDeleteAt(
            com.sanedge.pointofsale.pb.product.ProductResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return ProductResponseDeleteAt.builder()
                .id((long) p.getProductId())
                .merchantId(p.getMerchantId())
                .categoryId(p.getCategoryId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .countInStock(p.getCountInStock())
                .brand(p.getBrand())
                .weight(parseIntSafe(p.getWeight()))
                .rating(p.getRating())
                .slugProduct(p.getSlugProduct())
                .imageProduct(p.getImageProduct())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(p.getDeletedAt())
                .build();
    }

    public static List<ProductResponse> products(List<com.sanedge.pointofsale.pb.product.ProductResponse> list) {
        return list.stream().map(ProtoMapper::product).toList();
    }

    public static List<ProductResponseDeleteAt> productsDeleteAt(
            List<com.sanedge.pointofsale.pb.product.ProductResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::productDeleteAt).toList();
    }

    // ---- cashier ----
    public static CashierResponse cashier(com.sanedge.pointofsale.pb.cashier.CashierResponse p) {
        if (p == null) {
            return null;
        }
        return CashierResponse.builder()
                .id(p.getCashierId())
                .merchantId(p.getMerchantId())
                .name(p.getName())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static CashierResponseDeleteAt cashierDeleteAt(
            com.sanedge.pointofsale.pb.cashier.CashierResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return CashierResponseDeleteAt.builder()
                .id(p.getCashierId())
                .merchantId(p.getMerchantId())
                .name(p.getName())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(p.getDeletedAt())
                .build();
    }

    public static List<CashierResponse> cashiers(List<com.sanedge.pointofsale.pb.cashier.CashierResponse> list) {
        return list.stream().map(ProtoMapper::cashier).toList();
    }

    public static List<CashierResponseDeleteAt> cashiersDeleteAt(
            List<com.sanedge.pointofsale.pb.cashier.CashierResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::cashierDeleteAt).toList();
    }

    public static CashierResponseMonthSales cashierMonthSales(
            com.sanedge.pointofsale.pb.cashier.CashierResponseMonthSales p) {
        return CashierResponseMonthSales.builder()
                .month(p.getMonth())
                .cashierId(p.getCashierId())
                .cashierName(p.getCashierName())
                .orderCount(p.getOrderCount())
                .totalSales(p.getTotalSales())
                .build();
    }

    public static CashierResponseYearSales cashierYearSales(
            com.sanedge.pointofsale.pb.cashier.CashierResponseYearSales p) {
        return CashierResponseYearSales.builder()
                .year(p.getYear())
                .cashierId(p.getCashierId())
                .cashierName(p.getCashierName())
                .orderCount(p.getOrderCount())
                .totalSales(p.getTotalSales())
                .build();
    }

    public static CashierResponseMonthTotalSales cashierMonthTotalSales(
            com.sanedge.pointofsale.pb.cashier.CashierResponseMonthTotalSales p) {
        return CashierResponseMonthTotalSales.builder()
                .year(p.getYear())
                .month(p.getMonth())
                .totalSales(p.getTotalSales())
                .build();
    }

    public static CashierResponseYearTotalSales cashierYearTotalSales(
            com.sanedge.pointofsale.pb.cashier.CashierResponseYearTotalSales p) {
        return CashierResponseYearTotalSales.builder()
                .year(p.getYear())
                .totalSales(p.getTotalSales())
                .build();
    }

    // ---- order ----
    public static OrderResponse order(com.sanedge.pointofsale.pb.order.OrderResponse p) {
        if (p == null) {
            return null;
        }
        return OrderResponse.builder()
                .id((long) p.getOrderId())
                .merchantId(p.getMerchantId())
                .cashierId(p.getCashierId())
                .totalPrice(p.getTotalPrice())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static OrderResponseDeleteAt orderDeleteAt(com.sanedge.pointofsale.pb.order.OrderResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return OrderResponseDeleteAt.builder()
                .id((long) p.getOrderId())
                .merchantId(p.getMerchantId())
                .cashierId(p.getCashierId())
                .totalPrice(p.getTotalPrice())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(p.getDeletedAt())
                .build();
    }

    public static List<OrderResponse> orders(List<com.sanedge.pointofsale.pb.order.OrderResponse> list) {
        return list.stream().map(ProtoMapper::order).toList();
    }

    public static List<OrderResponseDeleteAt> ordersDeleteAt(
            List<com.sanedge.pointofsale.pb.order.OrderResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::orderDeleteAt).toList();
    }

    public static OrderMonthlyResponse orderMonthly(com.sanedge.pointofsale.pb.order.OrderMonthlyResponse p) {
        return OrderMonthlyResponse.builder()
                .month(p.getMonth())
                .orderCount(p.getOrderCount())
                .totalRevenue(p.getTotalRevenue())
                .totalItemsSold(p.getTotalItemsSold())
                .build();
    }

    public static OrderYearlyResponse orderYearly(com.sanedge.pointofsale.pb.order.OrderYearlyResponse p) {
        return OrderYearlyResponse.builder()
                .year(p.getYear())
                .orderCount(p.getOrderCount())
                .totalRevenue(p.getTotalRevenue())
                .totalItemsSold(p.getTotalItemsSold())
                .activeCashiers(p.getActiveCashiers())
                .uniqueProductsSold(p.getUniqueProductsSold())
                .build();
    }

    public static OrderMonthlyTotalRevenueResponse orderMonthlyTotalRevenue(
            com.sanedge.pointofsale.pb.order.OrderMonthlyTotalRevenueResponse p) {
        return OrderMonthlyTotalRevenueResponse.builder()
                .year(p.getYear())
                .month(p.getMonth())
                .totalRevenue(p.getTotalRevenue())
                .build();
    }

    public static OrderYearlyTotalRevenueResponse orderYearlyTotalRevenue(
            com.sanedge.pointofsale.pb.order.OrderYearlyTotalRevenueResponse p) {
        return OrderYearlyTotalRevenueResponse.builder()
                .year(p.getYear())
                .totalRevenue(p.getTotalRevenue())
                .build();
    }

    // ---- transaction ----
    public static TransactionResponse transaction(com.sanedge.pointofsale.pb.transaction.TransactionResponse p) {
        if (p == null) {
            return null;
        }
        return TransactionResponse.builder()
                .id((long) p.getTransactionId())
                .orderId(p.getOrderId())
                .merchantId(p.getMerchantId())
                .paymentMethod(p.getPaymentMethod())
                .amount(p.getAmount())
                .paymentStatus(p.getPaymentStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static TransactionResponseDeleteAt transactionDeleteAt(
            com.sanedge.pointofsale.pb.transaction.TransactionResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return TransactionResponseDeleteAt.builder()
                .id((long) p.getTransactionId())
                .orderId(p.getOrderId())
                .merchantId(p.getMerchantId())
                .paymentMethod(p.getPaymentMethod())
                .amount(p.getAmount())
                .paymentStatus(p.getPaymentStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(p.getDeletedAt())
                .build();
    }

    public static List<TransactionResponse> transactions(
            List<com.sanedge.pointofsale.pb.transaction.TransactionResponse> list) {
        return list.stream().map(ProtoMapper::transaction).toList();
    }

    public static List<TransactionResponseDeleteAt> transactionsDeleteAt(
            List<com.sanedge.pointofsale.pb.transaction.TransactionResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::transactionDeleteAt).toList();
    }

    public static TransactionMonthlyAmountSuccessResponse txnMonthlyAmountSuccess(
            com.sanedge.pointofsale.pb.transaction.TransactionMonthlyAmountSuccessResponse p) {
        return TransactionMonthlyAmountSuccessResponse.builder()
                .year(p.getYear())
                .month(p.getMonth())
                .totalSuccess(p.getTotalSuccess())
                .totalAmount(p.getTotalAmount())
                .build();
    }

    public static TransactionYearlyAmountSuccessResponse txnYearlyAmountSuccess(
            com.sanedge.pointofsale.pb.transaction.TransactionYearlyAmountSuccessResponse p) {
        return TransactionYearlyAmountSuccessResponse.builder()
                .year(p.getYear())
                .totalSuccess(p.getTotalSuccess())
                .totalAmount(p.getTotalAmount())
                .build();
    }

    public static TransactionMonthlyAmountFailedResponse txnMonthlyAmountFailed(
            com.sanedge.pointofsale.pb.transaction.TransactionMonthlyAmountFailedResponse p) {
        return TransactionMonthlyAmountFailedResponse.builder()
                .year(p.getYear())
                .month(p.getMonth())
                .totalFailed(p.getTotalFailed())
                .totalAmount(p.getTotalAmount())
                .build();
    }

    public static TransactionYearlyAmountFailedResponse txnYearlyAmountFailed(
            com.sanedge.pointofsale.pb.transaction.TransactionYearlyAmountFailedResponse p) {
        return TransactionYearlyAmountFailedResponse.builder()
                .year(p.getYear())
                .totalFailed(p.getTotalFailed())
                .totalAmount(p.getTotalAmount())
                .build();
    }

    public static TransactionMonthlyMethodResponse txnMonthlyMethod(
            com.sanedge.pointofsale.pb.transaction.TransactionMonthlyMethodResponse p) {
        return TransactionMonthlyMethodResponse.builder()
                .month(p.getMonth())
                .paymentMethod(p.getPaymentMethod())
                .totalTransactions(p.getTotalTransactions())
                .totalAmount(p.getTotalAmount())
                .build();
    }

    public static TransactionYearlyMethodResponse txnYearlyMethod(
            com.sanedge.pointofsale.pb.transaction.TransactionYearlyMethodResponse p) {
        return TransactionYearlyMethodResponse.builder()
                .year(p.getYear())
                .paymentMethod(p.getPaymentMethod())
                .totalTransactions(p.getTotalTransactions())
                .totalAmount(p.getTotalAmount())
                .build();
    }

    private static Integer parseIntSafe(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
