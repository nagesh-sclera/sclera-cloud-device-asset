FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
EXPOSE 8080
COPY target/sclera-api-gateway.jar app.jar
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
