FROM eclipse-temurin:21-jre-alpine

ENV TZ=UTC

WORKDIR /app

COPY build/libs/*.jar kindl-server.jar

ENTRYPOINT ["java", "-jar", "kindl-server.jar"]
