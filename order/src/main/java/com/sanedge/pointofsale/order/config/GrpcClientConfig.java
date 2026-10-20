package com.sanedge.pointofsale.order.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sanedge.pointofsale.common.adapter.cashier.CashierAdapter;
import com.sanedge.pointofsale.common.adapter.cashier.CashierQueryPort;
import com.sanedge.pointofsale.common.adapter.merchant.MerchantAdapter;
import com.sanedge.pointofsale.common.adapter.merchant.MerchantQueryPort;
import com.sanedge.pointofsale.common.adapter.order_item.OrderItemAdapter;
import com.sanedge.pointofsale.common.adapter.order_item.OrderItemCommandPort;
import com.sanedge.pointofsale.common.adapter.product.ProductAdapter;
import com.sanedge.pointofsale.common.adapter.product.ProductCommandPort;
import com.sanedge.pointofsale.common.adapter.product.ProductQueryPort;
import com.sanedge.pointofsale.common.resilience.DependencyGuardInterceptor;
import com.sanedge.pointofsale.pb.cashier.CashierQueryServiceGrpc;
import com.sanedge.pointofsale.pb.merchant.MerchantQueryServiceGrpc;
import com.sanedge.pointofsale.pb.order_item.OrderItemCommandServiceGrpc;
import com.sanedge.pointofsale.pb.order_item.OrderItemQueryServiceGrpc;
import com.sanedge.pointofsale.pb.product.ProductCommandServiceGrpc;
import com.sanedge.pointofsale.pb.product.ProductQueryServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import jakarta.annotation.PreDestroy;

/**
 * One plaintext {@link ManagedChannel} per downstream domain service plus the
 * blocking stubs used by the order command flow.
 */
@Configuration
public class GrpcClientConfig {

    private final DependencyGuardInterceptor dependencyGuardInterceptor;

    public GrpcClientConfig(DependencyGuardInterceptor dependencyGuardInterceptor) {
        this.dependencyGuardInterceptor = dependencyGuardInterceptor;
    }

    @Value("${grpc.client.merchant.host:localhost}")
    private String merchantHost;
    @Value("${grpc.client.merchant.port:9005}")
    private int merchantPort;

    @Value("${grpc.client.cashier.host:localhost}")
    private String cashierHost;
    @Value("${grpc.client.cashier.port:9014}")
    private int cashierPort;

    @Value("${grpc.client.product.host:localhost}")
    private String productHost;
    @Value("${grpc.client.product.port:9003}")
    private int productPort;

    @Value("${grpc.client.order-item.host:localhost}")
    private String orderItemHost;
    @Value("${grpc.client.order-item.port:9016}")
    private int orderItemPort;

    private final List<ManagedChannel> channels = new ArrayList<>();

    private ManagedChannel channel(String host, int port) {
        ManagedChannel channel = NettyChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .intercept(dependencyGuardInterceptor)
                .build();
        channels.add(channel);
        return channel;
    }

    @Bean
    public MerchantQueryServiceGrpc.MerchantQueryServiceBlockingStub merchantQueryStub() {
        return MerchantQueryServiceGrpc.newBlockingStub(channel(merchantHost, merchantPort));
    }

    @Bean
    public CashierQueryServiceGrpc.CashierQueryServiceBlockingStub cashierQueryStub() {
        return CashierQueryServiceGrpc.newBlockingStub(channel(cashierHost, cashierPort));
    }

    @Bean
    public ProductQueryServiceGrpc.ProductQueryServiceBlockingStub productQueryStub() {
        return ProductQueryServiceGrpc.newBlockingStub(channel(productHost, productPort));
    }

    @Bean
    public ProductCommandServiceGrpc.ProductCommandServiceBlockingStub productCommandStub() {
        return ProductCommandServiceGrpc.newBlockingStub(channel(productHost, productPort));
    }

    @Bean
    public OrderItemQueryServiceGrpc.OrderItemQueryServiceBlockingStub orderItemQueryStub() {
        return OrderItemQueryServiceGrpc.newBlockingStub(channel(orderItemHost, orderItemPort));
    }

    @Bean
    public OrderItemCommandServiceGrpc.OrderItemCommandServiceBlockingStub orderItemCommandStub() {
        return OrderItemCommandServiceGrpc.newBlockingStub(channel(orderItemHost, orderItemPort));
    }

    @Bean
    public MerchantQueryPort merchantQueryPort(
            MerchantQueryServiceGrpc.MerchantQueryServiceBlockingStub merchantQueryStub) {
        return new MerchantAdapter(merchantQueryStub);
    }

    @Bean
    public CashierQueryPort cashierQueryPort(
            CashierQueryServiceGrpc.CashierQueryServiceBlockingStub cashierQueryStub) {
        return new CashierAdapter(cashierQueryStub);
    }

    @Bean
    public ProductQueryPort productQueryPort(ProductQueryServiceGrpc.ProductQueryServiceBlockingStub productQueryStub,
            ProductCommandServiceGrpc.ProductCommandServiceBlockingStub productCommandStub) {
        return new ProductAdapter(productQueryStub, productCommandStub);
    }

    @Bean
    public ProductCommandPort productCommandPort(ProductQueryServiceGrpc.ProductQueryServiceBlockingStub productQueryStub,
            ProductCommandServiceGrpc.ProductCommandServiceBlockingStub productCommandStub) {
        return new ProductAdapter(productQueryStub, productCommandStub);
    }

    @Bean
    public OrderItemCommandPort orderItemCommandPort(
            OrderItemQueryServiceGrpc.OrderItemQueryServiceBlockingStub orderItemQueryStub,
            OrderItemCommandServiceGrpc.OrderItemCommandServiceBlockingStub orderItemCommandStub) {
        return new OrderItemAdapter(orderItemQueryStub, orderItemCommandStub);
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
