# syntax=docker/dockerfile:1.6

FROM gradle:8.8-jdk17 AS builder
WORKDIR /app
COPY settings.gradle.kts build.gradle.kts ./
COPY src ./src
RUN gradle clean bootJar -x test

FROM eclipse-temurin:17-jre-alpine
ENV APP_HOME=/app
WORKDIR ${APP_HOME}
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
