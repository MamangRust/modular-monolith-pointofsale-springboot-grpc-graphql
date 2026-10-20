package com.sanedge.pointofsale.common.chaos;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers {@link ChaosHttpInterceptor} for every route.
 *
 * <p>Spring port of the Quarkus {@code ChaosRouteFilter}, which observed the
 * Vert.x {@code Router} and attached the middleware with {@code order(-100)}.
 * The Spring equivalent is a {@link WebMvcConfigurer} contributing an
 * interceptor; {@code order(-100)} is preserved so chaos runs before other
 * interceptors.
 */
@Configuration
@ConditionalOnClass(HandlerInterceptor.class)
@ConditionalOnProperty(name = "chaos.http.enabled", havingValue = "true", matchIfMissing = true)
public class ChaosWebMvcConfig implements WebMvcConfigurer {

    private final ChaosHttpInterceptor chaosHttpInterceptor;

    public ChaosWebMvcConfig(ChaosHttpInterceptor chaosHttpInterceptor) {
        this.chaosHttpInterceptor = chaosHttpInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(chaosHttpInterceptor)
                .addPathPatterns("/**")
                .order(-100);
    }
}
