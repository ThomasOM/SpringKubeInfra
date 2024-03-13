FROM openjdk:17-jdk-alpine
EXPOSE 8080
COPY target/api-gateway-1.0.jar api-gateway.jar
ENTRYPOINT ["java","-jar","api-gateway.jar"]