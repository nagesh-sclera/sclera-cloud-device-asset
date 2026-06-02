FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY target/sclera-scheduler-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8098
ENV JAVA_OPTS="-Xmx128m -Xss512k"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
