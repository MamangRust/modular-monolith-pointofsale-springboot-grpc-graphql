package com.sanedge.pointofsale.cashier.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sanedge.pointofsale.common.adapter.merchant.MerchantAdapter;
import com.sanedge.pointofsale.common.adapter.merchant.MerchantQueryPort;
import com.sanedge.pointofsale.common.adapter.user.UserAdapter;
import com.sanedge.pointofsale.common.adapter.user.UserQueryPort;
import com.sanedge.pointofsale.common.resilience.DependencyGuardInterceptor;
import com.sanedge.pointofsale.pb.merchant.MerchantQueryServiceGrpc;
import com.sanedge.pointofsale.pb.user.UserCommandServiceGrpc;
import com.sanedge.pointofsale.pb.user.UserQueryServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

/** Outbound gRPC channels/stubs used by the cashier service. */
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

    @Value("${grpc.client.user.host:localhost}")
    private String userHost;

    @Value("${grpc.client.user.port:9011}")
    private int userPort;

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel merchantChannel() {
        return NettyChannelBuilder.forAddress(merchantHost, merchantPort)
                .usePlaintext()
                .intercept(dependencyGuardInterceptor)
                .build();
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel userChannel() {
        return NettyChannelBuilder.forAddress(userHost, userPort)
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
    public UserQueryServiceGrpc.UserQueryServiceBlockingStub userQueryStub(
            @Qualifier("userChannel") ManagedChannel userChannel) {
        return UserQueryServiceGrpc.newBlockingStub(userChannel);
    }

    @Bean
    public UserCommandServiceGrpc.UserCommandServiceBlockingStub userCommandStub(
            @Qualifier("userChannel") ManagedChannel userChannel) {
        return UserCommandServiceGrpc.newBlockingStub(userChannel);
    }

    @Bean
    public MerchantQueryPort merchantQueryPort(
            MerchantQueryServiceGrpc.MerchantQueryServiceBlockingStub merchantQueryStub) {
        return new MerchantAdapter(merchantQueryStub);
    }

    @Bean
    public UserQueryPort userQueryPort(UserQueryServiceGrpc.UserQueryServiceBlockingStub userQueryStub,
            UserCommandServiceGrpc.UserCommandServiceBlockingStub userCommandStub) {
        return new UserAdapter(userQueryStub, userCommandStub);
    }
}
