package com.sanedge.pointofsale.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sanedge.pointofsale.common.adapter.role.RoleAdapter;
import com.sanedge.pointofsale.common.adapter.role.RoleQueryPort;
import com.sanedge.pointofsale.common.resilience.DependencyGuardInterceptor;
import com.sanedge.pointofsale.pb.role.RoleQueryServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

@Configuration
public class GrpcClientConfig {

    private final DependencyGuardInterceptor dependencyGuardInterceptor;

    public GrpcClientConfig(DependencyGuardInterceptor dependencyGuardInterceptor) {
        this.dependencyGuardInterceptor = dependencyGuardInterceptor;
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel roleChannel(
            @Value("${grpc.client.role.host:localhost}") String host,
            @Value("${grpc.client.role.port:9006}") int port) {
        return NettyChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .intercept(dependencyGuardInterceptor)
                .build();
    }

    @Bean
    public RoleQueryServiceGrpc.RoleQueryServiceBlockingStub roleQueryServiceBlockingStub(ManagedChannel roleChannel) {
        return RoleQueryServiceGrpc.newBlockingStub(roleChannel);
    }

    @Bean
    public RoleQueryPort roleQueryPort(RoleQueryServiceGrpc.RoleQueryServiceBlockingStub roleQueryServiceBlockingStub) {
        return new RoleAdapter(roleQueryServiceBlockingStub);
    }
}
