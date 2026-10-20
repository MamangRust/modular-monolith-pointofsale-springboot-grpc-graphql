package com.sanedge.pointofsale.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import graphql.scalars.ExtendedScalars;

/**
 * Registers the {@code Long} scalar used by the schema (e.g. {@code totalItems},
 * {@code totalRevenue}) so that 64-bit values are not truncated to Int.
 */
@Configuration
public class GraphQlConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder.scalar(ExtendedScalars.GraphQLLong);
    }
}
