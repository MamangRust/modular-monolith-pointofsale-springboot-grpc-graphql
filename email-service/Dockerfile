# syntax=docker/dockerfile:1.7

# ---------- Stage 1: build ----------
# Build context is the pointofsale/ multi-module root:
#   docker build -f email-service/Dockerfile -t quarkus-grpc-pointofsale/email-service:latest .
# NOTE: the build stage must be glibc-based (Debian/Ubuntu), NOT Alpine.
# The protobuf-maven-plugin downloads glibc-linked native protoc +
# protoc-gen-grpc-java binaries that cannot execute on musl libc.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Pre-fetch dependencies for the target module + its reactor deps (caches ~/.m2).
# Every module pom must be present because the root pom declares the full reactor.
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./
COPY common/pom.xml common/
COPY auth/pom.xml auth/
COPY user/pom.xml user/
COPY role/pom.xml role/
COPY merchant/pom.xml merchant/
COPY cashier/pom.xml cashier/
COPY category/pom.xml category/
COPY product/pom.xml product/
COPY order/pom.xml order/
COPY order-item/pom.xml order-item/
COPY transaction/pom.xml transaction/
COPY gateway/pom.xml gateway/
COPY email-service/pom.xml email-service/
COPY db-migration/pom.xml db-migration/
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl email-service -am dependency:go-offline

# Copy sources and package the fat jar.
COPY common/src common/src
COPY email-service/src email-service/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl email-service -am -DskipTests package

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Non-root runtime user (UID 1001, matches the k8s securityContext).
RUN addgroup -g 1001 -S app \
    && adduser -u 1001 -S app -G app \
    && mkdir -p /var/log/app \
    && chown -R 1001:1001 /app /var/log/app

COPY --from=build --chown=1001:1001 \
    /workspace/email-service/target/email-service-0.0.1-SNAPSHOT.jar /app/app.jar

USER 1001:1001

# HTTP server port (server.port) — actuator health/metrics surface.
EXPOSE 8098

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
