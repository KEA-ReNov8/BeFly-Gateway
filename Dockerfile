FROM openjdk:17-jdk-slim

# JAR 파일 복사 및 실행
ARG JAR_FILE=build/libs/Befly-Gateway-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENV SPRING_PROFILES_ACTIVE=dev

ENTRYPOINT ["java","-jar","app.jar"]