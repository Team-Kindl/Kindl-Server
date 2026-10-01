FROM eclipse-temurin:21-jre-alpine

ENV TZ=UTC \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Duser.timezone=UTC"

WORKDIR /app

COPY kindl-api/build/libs/kindl-api.jar kindl-server.jar

ENTRYPOINT ["java", "-jar", "kindl-server.jar"]
