FROM gradle:8-jdk17-alpine AS builder
WORKDIR /home/gradle/project

COPY settings.gradle build.gradle gradle.* ./
RUN gradle clean bootJar --no-daemon

COPY src ./src
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:17-jdk-slim
WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /home/gradle/project/${JAR_FILE} app.jar

ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]