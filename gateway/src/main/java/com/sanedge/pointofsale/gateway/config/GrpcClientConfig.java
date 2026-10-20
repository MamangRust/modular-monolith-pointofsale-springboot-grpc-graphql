package com.sanedge.pointofsale.gateway.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sanedge.pointofsale.pb.auth.AuthServiceGrpc;
import com.sanedge.pointofsale.pb.cashier.CashierCommandServiceGrpc;
import com.sanedge.pointofsale.pb.cashier.CashierQueryServiceGrpc;
import com.sanedge.pointofsale.pb.cashier.stats.CashierSalesServiceGrpc;
import com.sanedge.pointofsale.pb.cashier.stats.CashierTotalSalesServiceGrpc;
import com.sanedge.pointofsale.pb.category.CategoryCommandServiceGrpc;
import com.sanedge.pointofsale.pb.category.CategoryQueryServiceGrpc;
import com.sanedge.pointofsale.pb.category.stats.CategoryPriceServiceGrpc;
import com.sanedge.pointofsale.pb.category.stats.CategoryTotalPriceServiceGrpc;
import com.sanedge.pointofsale.pb.merchant.MerchantCommandServiceGrpc;
import com.sanedge.pointofsale.pb.merchant.MerchantQueryServiceGrpc;
import com.sanedge.pointofsale.pb.order.OrderCommandServiceGrpc;
import com.sanedge.pointofsale.pb.order.OrderQueryServiceGrpc;
import com.sanedge.pointofsale.pb.order.stats.OrderSoldoutServiceGrpc;
import com.sanedge.pointofsale.pb.order.stats.OrderTotalRevenueServiceGrpc;
import com.sanedge.pointofsale.pb.order_item.OrderItemCommandServiceGrpc;
import com.sanedge.pointofsale.pb.order_item.OrderItemQueryServiceGrpc;
import com.sanedge.pointofsale.pb.product.ProductCommandServiceGrpc;
import com.sanedge.pointofsale.pb.product.ProductQueryServiceGrpc;
import com.sanedge.pointofsale.pb.role.RoleCommandServiceGrpc;
import com.sanedge.pointofsale.pb.role.RoleQueryServiceGrpc;
import com.sanedge.pointofsale.pb.transaction.TransactionCommandServiceGrpc;
import com.sanedge.pointofsale.pb.transaction.TransactionQueryServiceGrpc;
import com.sanedge.pointofsale.pb.transaction.stats.TransactionStatsAmountServiceGrpc;
import com.sanedge.pointofsale.pb.transaction.stats.TransactionStatsMethodServiceGrpc;
import com.sanedge.pointofsale.pb.user.UserCommandServiceGrpc;
import com.sanedge.pointofsale.pb.user.UserQueryServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import jakarta.annotation.PreDestroy;

/**
 * One plaintext {@link ManagedChannel} per downstream domain service plus the
 * blocking stubs used by the REST controllers.
 */
@Configuration
public class GrpcClientConfig {

    @Value("${grpc.client.auth.host:localhost}")
    private String authHost;
    @Value("${grpc.client.auth.port:9012}")
    private int authPort;

    @Value("${grpc.client.user.host:localhost}")
    private String userHost;
    @Value("${grpc.client.user.port:9011}")
    private int userPort;

    @Value("${grpc.client.role.host:localhost}")
    private String roleHost;
    @Value("${grpc.client.role.port:9006}")
    private int rolePort;

    @Value("${grpc.client.merchant.host:localhost}")
    private String merchantHost;
    @Value("${grpc.client.merchant.port:9005}")
    private int merchantPort;

    @Value("${grpc.client.cashier.host:localhost}")
    private String cashierHost;
    @Value("${grpc.client.cashier.port:9014}")
    private int cashierPort;

    @Value("${grpc.client.category.host:localhost}")
    private String categoryHost;
    @Value("${grpc.client.category.port:9015}")
    private int categoryPort;

    @Value("${grpc.client.product.host:localhost}")
    private String productHost;
    @Value("${grpc.client.product.port:9003}")
    private int productPort;

    @Value("${grpc.client.order.host:localhost}")
    private String orderHost;
    @Value("${grpc.client.order.port:9001}")
    private int orderPort;

    @Value("${grpc.client.order-item.host:localhost}")
    private String orderItemHost;
    @Value("${grpc.client.order-item.port:9016}")
    private int orderItemPort;

    @Value("${grpc.client.transaction.host:localhost}")
    private String transactionHost;
    @Value("${grpc.client.transaction.port:9009}")
    private int transactionPort;

    private final java.util.List<ManagedChannel> channels = new java.util.ArrayList<>();

    private ManagedChannel channel(String host, int port) {
        ManagedChannel channel = NettyChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        channels.add(channel);
        return channel;
    }

    // --- auth ---
    @Bean
    public AuthServiceGrpc.AuthServiceBlockingStub authStub() {
        return AuthServiceGrpc.newBlockingStub(channel(authHost, authPort));
    }

    // --- user ---
    @Bean
    public UserQueryServiceGrpc.UserQueryServiceBlockingStub userQueryStub() {
        return UserQueryServiceGrpc.newBlockingStub(channel(userHost, userPort));
    }

    @Bean
    public UserCommandServiceGrpc.UserCommandServiceBlockingStub userCommandStub() {
        return UserCommandServiceGrpc.newBlockingStub(channel(userHost, userPort));
    }

    // --- role ---
    @Bean
    public RoleQueryServiceGrpc.RoleQueryServiceBlockingStub roleQueryStub() {
        return RoleQueryServiceGrpc.newBlockingStub(channel(roleHost, rolePort));
    }

    @Bean
    public RoleCommandServiceGrpc.RoleCommandServiceBlockingStub roleCommandStub() {
        return RoleCommandServiceGrpc.newBlockingStub(channel(roleHost, rolePort));
    }

    // --- merchant ---
    @Bean
    public MerchantQueryServiceGrpc.MerchantQueryServiceBlockingStub merchantQueryStub() {
        return MerchantQueryServiceGrpc.newBlockingStub(channel(merchantHost, merchantPort));
    }

    @Bean
    public MerchantCommandServiceGrpc.MerchantCommandServiceBlockingStub merchantCommandStub() {
        return MerchantCommandServiceGrpc.newBlockingStub(channel(merchantHost, merchantPort));
    }

    // --- cashier ---
    @Bean
    public CashierQueryServiceGrpc.CashierQueryServiceBlockingStub cashierQueryStub() {
        return CashierQueryServiceGrpc.newBlockingStub(channel(cashierHost, cashierPort));
    }

    @Bean
    public CashierCommandServiceGrpc.CashierCommandServiceBlockingStub cashierCommandStub() {
        return CashierCommandServiceGrpc.newBlockingStub(channel(cashierHost, cashierPort));
    }

    @Bean
    public CashierSalesServiceGrpc.CashierSalesServiceBlockingStub cashierSalesStub() {
        return CashierSalesServiceGrpc.newBlockingStub(channel(cashierHost, cashierPort));
    }

    @Bean
    public CashierTotalSalesServiceGrpc.CashierTotalSalesServiceBlockingStub cashierTotalSalesStub() {
        return CashierTotalSalesServiceGrpc.newBlockingStub(channel(cashierHost, cashierPort));
    }

    // --- category ---
    @Bean
    public CategoryQueryServiceGrpc.CategoryQueryServiceBlockingStub categoryQueryStub() {
        return CategoryQueryServiceGrpc.newBlockingStub(channel(categoryHost, categoryPort));
    }

    @Bean
    public CategoryCommandServiceGrpc.CategoryCommandServiceBlockingStub categoryCommandStub() {
        return CategoryCommandServiceGrpc.newBlockingStub(channel(categoryHost, categoryPort));
    }

    @Bean
    public CategoryPriceServiceGrpc.CategoryPriceServiceBlockingStub categoryPriceStub() {
        return CategoryPriceServiceGrpc.newBlockingStub(channel(categoryHost, categoryPort));
    }

    @Bean
    public CategoryTotalPriceServiceGrpc.CategoryTotalPriceServiceBlockingStub categoryTotalPriceStub() {
        return CategoryTotalPriceServiceGrpc.newBlockingStub(channel(categoryHost, categoryPort));
    }

    // --- product ---
    @Bean
    public ProductQueryServiceGrpc.ProductQueryServiceBlockingStub productQueryStub() {
        return ProductQueryServiceGrpc.newBlockingStub(channel(productHost, productPort));
    }

    @Bean
    public ProductCommandServiceGrpc.ProductCommandServiceBlockingStub productCommandStub() {
        return ProductCommandServiceGrpc.newBlockingStub(channel(productHost, productPort));
    }

    // --- order ---
    @Bean
    public OrderQueryServiceGrpc.OrderQueryServiceBlockingStub orderQueryStub() {
        return OrderQueryServiceGrpc.newBlockingStub(channel(orderHost, orderPort));
    }

    @Bean
    public OrderCommandServiceGrpc.OrderCommandServiceBlockingStub orderCommandStub() {
        return OrderCommandServiceGrpc.newBlockingStub(channel(orderHost, orderPort));
    }

    @Bean
    public OrderSoldoutServiceGrpc.OrderSoldoutServiceBlockingStub orderSoldoutStub() {
        return OrderSoldoutServiceGrpc.newBlockingStub(channel(orderHost, orderPort));
    }

    @Bean
    public OrderTotalRevenueServiceGrpc.OrderTotalRevenueServiceBlockingStub orderTotalRevenueStub() {
        return OrderTotalRevenueServiceGrpc.newBlockingStub(channel(orderHost, orderPort));
    }

    // --- order-item ---
    @Bean
    public OrderItemQueryServiceGrpc.OrderItemQueryServiceBlockingStub orderItemQueryStub() {
        return OrderItemQueryServiceGrpc.newBlockingStub(channel(orderItemHost, orderItemPort));
    }

    @Bean
    public OrderItemCommandServiceGrpc.OrderItemCommandServiceBlockingStub orderItemCommandStub() {
        return OrderItemCommandServiceGrpc.newBlockingStub(channel(orderItemHost, orderItemPort));
    }

    // --- transaction ---
    @Bean
    public TransactionQueryServiceGrpc.TransactionQueryServiceBlockingStub transactionQueryStub() {
        return TransactionQueryServiceGrpc.newBlockingStub(channel(transactionHost, transactionPort));
    }

    @Bean
    public TransactionCommandServiceGrpc.TransactionCommandServiceBlockingStub transactionCommandStub() {
        return TransactionCommandServiceGrpc.newBlockingStub(channel(transactionHost, transactionPort));
    }

    @Bean
    public TransactionStatsAmountServiceGrpc.TransactionStatsAmountServiceBlockingStub transactionStatsAmountStub() {
        return TransactionStatsAmountServiceGrpc.newBlockingStub(channel(transactionHost, transactionPort));
    }

    @Bean
    public TransactionStatsMethodServiceGrpc.TransactionStatsMethodServiceBlockingStub transactionStatsMethodStub() {
        return TransactionStatsMethodServiceGrpc.newBlockingStub(channel(transactionHost, transactionPort));
    }

    @PreDestroy
    public void shutdown() {
        for (ManagedChannel channel : channels) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
