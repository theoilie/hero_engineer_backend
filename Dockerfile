FROM openjdk:14-jdk-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG APPJAR=build/libs/*.jar
COPY ${APPJAR} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]