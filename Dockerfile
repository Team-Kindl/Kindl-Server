FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/*.jar kindl-server.jar

ENTRYPOINT ["java", "-jar", "kindl-server.jar"]
