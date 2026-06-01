FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY target/sclera-inventory-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8093
ENV JAVA_OPTS="-Xmx128m -Xss512k"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
