FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY target/sclera.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/app.jar"]
