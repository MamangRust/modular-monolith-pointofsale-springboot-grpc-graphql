package com.sanedge.pointofsale.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sanedge.pointofsale.common.adapter.role.RoleAdapter;
import com.sanedge.pointofsale.common.adapter.role.RoleQueryPort;
import com.sanedge.pointofsale.common.adapter.user.UserAdapter;
import com.sanedge.pointofsale.common.adapter.user.UserCommandPort;
import com.sanedge.pointofsale.common.adapter.user.UserQueryPort;
import com.sanedge.pointofsale.common.resilience.DependencyGuardInterceptor;
import com.sanedge.pointofsale.pb.role.RoleQueryServiceGrpc;
import com.sanedge.pointofsale.pb.user.UserCommandServiceGrpc;
import com.sanedge.pointofsale.pb.user.UserQueryServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

/** Outbound gRPC channels/stubs used by the auth service. */
@Configuration
public class GrpcClientConfig {

    private final DependencyGuardInterceptor dependencyGuardInterceptor;

    public GrpcClientConfig(DependencyGuardInterceptor dependencyGuardInterceptor) {
        this.dependencyGuardInterceptor = dependencyGuardInterceptor;
    }

    @Value("${grpc.client.user.host:localhost}")
    private String userHost;

    @Value("${grpc.client.user.port:9011}")
    private int userPort;

    @Value("${grpc.client.role.host:localhost}")
    private String roleHost;

    @Value("${grpc.client.role.port:9006}")
    private int rolePort;

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel userChannel() {
        return NettyChannelBuilder.forAddress(userHost, userPort)
                .usePlaintext()
                .intercept(dependencyGuardInterceptor)
                .build();
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel roleChannel() {
        return NettyChannelBuilder.forAddress(roleHost, rolePort)
                .usePlaintext()
                .intercept(dependencyGuardInterceptor)
                .build();
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
    public RoleQueryServiceGrpc.RoleQueryServiceBlockingStub roleQueryStub(
            @Qualifier("roleChannel") ManagedChannel roleChannel) {
        return RoleQueryServiceGrpc.newBlockingStub(roleChannel);
    }

    @Bean
    public UserQueryPort userQueryPort(UserQueryServiceGrpc.UserQueryServiceBlockingStub userQueryStub,
            UserCommandServiceGrpc.UserCommandServiceBlockingStub userCommandStub) {
        return new UserAdapter(userQueryStub, userCommandStub);
    }

    @Bean
    public UserCommandPort userCommandPort(UserQueryServiceGrpc.UserQueryServiceBlockingStub userQueryStub,
            UserCommandServiceGrpc.UserCommandServiceBlockingStub userCommandStub) {
        return new UserAdapter(userQueryStub, userCommandStub);
    }

    @Bean
    public RoleQueryPort roleQueryPort(RoleQueryServiceGrpc.RoleQueryServiceBlockingStub roleQueryStub) {
        return new RoleAdapter(roleQueryStub);
    }
}
