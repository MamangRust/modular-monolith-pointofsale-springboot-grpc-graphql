package com.sanedge.pointofsale.product.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sanedge.pointofsale.common.adapter.merchant.MerchantAdapter;
import com.sanedge.pointofsale.common.adapter.merchant.MerchantQueryPort;
import com.sanedge.pointofsale.common.resilience.DependencyGuardInterceptor;
import com.sanedge.pointofsale.pb.category.CategoryQueryServiceGrpc;
import com.sanedge.pointofsale.pb.merchant.MerchantQueryServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

/** Outbound gRPC channels/stubs used by the product service. */
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

    @Value("${grpc.client.category.host:localhost}")
    private String categoryHost;

    @Value("${grpc.client.category.port:9015}")
    private int categoryPort;

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel merchantChannel() {
        return NettyChannelBuilder.forAddress(merchantHost, merchantPort)
                .usePlaintext()
                .intercept(dependencyGuardInterceptor)
                .build();
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel categoryChannel() {
        return NettyChannelBuilder.forAddress(categoryHost, categoryPort)
                .usePlaintext()
                .intercept(dependencyGuardInterceptor)
                .build();
    }

    @Bean
    public MerchantQueryServiceGrpc.MerchantQueryServiceBlockingStub merchantQueryStub(
            @Qualifier("merchantChannel") ManagedChannel merchantChannel) {
        return MerchantQueryServiceGrpc.newBlockingStub(merchantChannel);
    }

    @Bean
    public CategoryQueryServiceGrpc.CategoryQueryServiceBlockingStub categoryQueryStub(
            @Qualifier("categoryChannel") ManagedChannel categoryChannel) {
        return CategoryQueryServiceGrpc.newBlockingStub(categoryChannel);
    }

    @Bean
    public MerchantQueryPort merchantQueryPort(
            MerchantQueryServiceGrpc.MerchantQueryServiceBlockingStub merchantQueryStub) {
        return new MerchantAdapter(merchantQueryStub);
    }
}
