FROM openjdk:17-jdk-alpine
EXPOSE 8080
COPY target/user-service-1.0.jar user-service.jar
ENTRYPOINT ["java","-jar","user-service.jar"]