FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/sclera-integrations-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8096
ENV JAVA_OPTS="-Xmx128m -Xss512k"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
