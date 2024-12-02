FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

FROM openjdk:17-jdk
WORKDIR /app
COPY --from=build /app/target/greet.jar greet.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/greet.jar"]