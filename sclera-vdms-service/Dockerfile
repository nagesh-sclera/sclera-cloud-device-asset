FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
EXPOSE 8089
COPY target/sclera-vdms-service.jar app.jar
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

