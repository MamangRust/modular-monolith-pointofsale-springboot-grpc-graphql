package com.sanedge.pointofsale.gateway.config;

import java.util.List;
import java.util.Map;

import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Translates downstream gRPC failures and security/validation exceptions into
 * structured GraphQL errors, replacing the former REST {@code @ExceptionHandler}
 * layer. Business {@code status}/{@code message} signalling from the proto
 * envelope is preserved inside the {@code ApiResponse} payloads themselves.
 */
@Slf4j
@Component
public class GraphQlExceptionResolver implements DataFetcherExceptionResolver {

    @Override
    public Mono<List<GraphQLError>> resolveException(Throwable exception, DataFetchingEnvironment env) {
        if (exception instanceof StatusRuntimeException sre) {
            log.error("gRPC call failed: {} - {}", sre.getStatus().getCode(), sre.getStatus().getDescription());
            return Mono.just(List.of(build(env, sre.getStatus())));
        }
        if (exception instanceof AccessDeniedException) {
            return Mono.just(List.of(GraphqlErrorBuilder.newError(env)
                    .message("Access denied")
                    .errorType(ErrorType.FORBIDDEN)
                    .extensions(Map.of("code", "PERMISSION_DENIED"))
                    .build()));
        }
        if (exception instanceof ConstraintViolationException cve) {
            return Mono.just(List.of(GraphqlErrorBuilder.newError(env)
                    .message(cve.getMessage())
                    .errorType(ErrorType.BAD_REQUEST)
                    .extensions(Map.of("code", "BAD_USER_INPUT"))
                    .build()));
        }
        return Mono.empty();
    }

    private GraphQLError build(DataFetchingEnvironment env, Status status) {
        ErrorType errorType = switch (status.getCode()) {
            case NOT_FOUND -> ErrorType.NOT_FOUND;
            case INVALID_ARGUMENT, FAILED_PRECONDITION, ALREADY_EXISTS -> ErrorType.BAD_REQUEST;
            case PERMISSION_DENIED, UNAUTHENTICATED -> ErrorType.FORBIDDEN;
            case UNAVAILABLE, DEADLINE_EXCEEDED -> ErrorType.INTERNAL_ERROR;
            default -> ErrorType.INTERNAL_ERROR;
        };
        String message = status.getDescription() != null ? status.getDescription() : status.getCode().name();
        return GraphqlErrorBuilder.newError(env)
                .message(message)
                .errorType(errorType)
                .extensions(Map.of("code", errorType.name(), "grpcStatus", status.getCode().name()))
                .build();
    }
}
