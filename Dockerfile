FROM openjdk:17-jdk-slim

ARG JAR_FILE=build/libs/Befly-Gateway-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 443

ENTRYPOINT ["java","-jar","app.jar"]
