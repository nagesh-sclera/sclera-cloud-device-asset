FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY target/sclera-audit-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8090
ENV JAVA_OPTS="-Xmx128m -Xss512k"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
