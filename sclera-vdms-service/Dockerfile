# syntax=docker/dockerfile:1
# Build context is the repo ROOT (see docker-compose.yml build.context).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -DskipTests -pl sclera-vdms-service -am package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
EXPOSE 8089
COPY --from=build /build/sclera-vdms-service/target/sclera-vdms-service.jar app.jar
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
