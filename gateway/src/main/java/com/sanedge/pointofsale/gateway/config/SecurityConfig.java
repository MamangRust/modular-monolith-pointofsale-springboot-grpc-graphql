package com.sanedge.pointofsale.gateway.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.sanedge.pointofsale.gateway.security.AuthAccessDenied;
import com.sanedge.pointofsale.gateway.security.AuthTokenEntryPoint;
import com.sanedge.pointofsale.gateway.security.AuthTokenFilter;
import com.sanedge.pointofsale.gateway.security.UserDetailImplService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        @Autowired
        UserDetailImplService userService;

        @Autowired
        private AuthTokenEntryPoint unauthorizedHandler;

        @Autowired
        private AuthAccessDenied authAccessDenied;

        @Autowired
        private AuthTokenFilter authTokenFilter;

        private static final String[] PUBLIC_ENDPOINTS = {
                        "/graphql",
                        "/graphiql",
                        "/graphiql/**",
                        "/static/**",
                        "/error"
        };

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
                        throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http)
                        throws Exception {
                return http
                                .cors(Customizer.withDefaults())
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(
                                                sessionManager -> sessionManager
                                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(req -> req
                                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                                .anyRequest().authenticated())
                                .userDetailsService(userService)
                                .anonymous(AbstractHttpConfigurer::disable)
                                .exceptionHandling(handler -> handler
                                                .accessDeniedHandler(authAccessDenied)
                                                .authenticationEntryPoint(unauthorizedHandler))
                                .addFilterBefore(
                                                authTokenFilter,
                                                UsernamePasswordAuthenticationFilter.class)
                                .build();
        }

        @Bean
        public CorsConfigurationSource corsConfiguration() {
                var configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(List.of("*"));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                configuration.setExposedHeaders(List.of("Authorization")); // Expose Authorization header

                var source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}