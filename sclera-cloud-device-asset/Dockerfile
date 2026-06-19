# syntax=docker/dockerfile:1
# Build context is the repo ROOT (see docker-compose.yml build.context).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -DskipTests -pl sclera-cloud-device-asset -am package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
EXPOSE 8085
# Image store served back by ResourceConfigs (/images/** -> file:/home/sclera/images/).
# addFileToServer() uses Files.write, which does not create parent dirs, so the asset image
# directories must exist. Created here so they also seed a fresh named volume on first mount.
RUN mkdir -p /home/sclera/images/assets/ocr /home/sclera/images/document
COPY --from=build /build/sclera-cloud-device-asset/target/sclera.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
