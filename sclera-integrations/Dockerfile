# syntax=docker/dockerfile:1
# Build context is the repo ROOT (see docker-compose.yml build.context).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -DskipTests -pl sclera-integrations -am package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
EXPOSE 8096
COPY --from=build /build/sclera-integrations/target/sclera-integrations-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_OPTS="-Xmx128m -Xss512k"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
