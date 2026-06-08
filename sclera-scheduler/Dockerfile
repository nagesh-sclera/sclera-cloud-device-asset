# --- build stage: builds dapr-commons + sclera-scheduler from source ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
# dapr-commons is a local dependency; install it first
COPY dapr-commons /src/dapr-commons
RUN mvn -q -f /src/dapr-commons/pom.xml -DskipTests install
COPY sclera-scheduler /src/sclera-scheduler
RUN mvn -q -f /src/sclera-scheduler/pom.xml -DskipTests package

# --- runtime stage ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /src/sclera-scheduler/target/sclera-scheduler-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8098
ENV JAVA_OPTS="-Xmx128m -Xss512k"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
