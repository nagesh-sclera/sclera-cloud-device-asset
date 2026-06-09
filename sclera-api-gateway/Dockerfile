# syntax=docker/dockerfile:1
# Build context is the repo ROOT (see docker-compose.yml build.context).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -DskipTests -pl sclera-api-gateway -am package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
EXPOSE 8080
COPY --from=build /build/sclera-api-gateway/target/sclera-api-gateway.jar app.jar
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
