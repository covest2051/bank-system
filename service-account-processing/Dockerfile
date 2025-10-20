FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Копируем jar (он собирается через mvn clean package)
COPY target/service-account-processing-1.0-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
