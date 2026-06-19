# syntax=docker/dockerfile:1
#
# Multi-stage build mirroring the order-service / inventory-service pattern
# from dapr-spring-ak: Maven build → eclipse-temurin:21-jre-alpine runtime,
# non-root "spring" user, JAR named after the module.

# ──────── Build stage ──────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Cache the dependency layer
COPY pom.xml ./
RUN mvn -B -e -ntp -q dependency:go-offline

# Build the JAR
COPY src ./src
RUN mvn -B -e -ntp -q clean package -Dmaven.test.skip=true

# ──────── Runtime stage ────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /build/target/sclera-cloud-workorder-1.0.0.jar /app/app.jar
# Log directory for logback file appenders (LOG_PATH=/home/sclera/logs/...)
RUN mkdir -p /home/sclera/logs/sclera_backend/services \
    && chown -R spring:spring /app /home/sclera
USER spring:spring

EXPOSE 8094
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
