package com.sanedge.pointofsale.transaction.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sanedge.pointofsale.common.adapter.merchant.MerchantAdapter;
import com.sanedge.pointofsale.common.adapter.merchant.MerchantQueryPort;
import com.sanedge.pointofsale.common.adapter.order.OrderAdapter;
import com.sanedge.pointofsale.common.adapter.order.OrderQueryPort;
import com.sanedge.pointofsale.common.adapter.order_item.OrderItemAdapter;
import com.sanedge.pointofsale.common.adapter.order_item.OrderItemQueryPort;
import com.sanedge.pointofsale.common.resilience.DependencyGuardInterceptor;
import com.sanedge.pointofsale.pb.merchant.MerchantQueryServiceGrpc;
import com.sanedge.pointofsale.pb.order.OrderQueryServiceGrpc;
import com.sanedge.pointofsale.pb.order_item.OrderItemCommandServiceGrpc;
import com.sanedge.pointofsale.pb.order_item.OrderItemQueryServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import jakarta.annotation.PreDestroy;

/**
 * One plaintext {@link ManagedChannel} per downstream domain service plus the
 * blocking stubs used by the transaction command flow.
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

    @Value("${grpc.client.order.host:localhost}")
    private String orderHost;
    @Value("${grpc.client.order.port:9001}")
    private int orderPort;

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
    public OrderQueryServiceGrpc.OrderQueryServiceBlockingStub orderQueryStub() {
        return OrderQueryServiceGrpc.newBlockingStub(channel(orderHost, orderPort));
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
    public OrderQueryPort orderQueryPort(OrderQueryServiceGrpc.OrderQueryServiceBlockingStub orderQueryStub) {
        return new OrderAdapter(orderQueryStub);
    }

    @Bean
    public OrderItemQueryPort orderItemQueryPort(
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
