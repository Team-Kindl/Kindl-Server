FROM eclipse-temurin:21-jre-alpine

ENV TZ=UTC \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Duser.timezone=UTC"

WORKDIR /app

COPY api/build/libs/proov-api.jar proov-api.jar

ENTRYPOINT ["java", "-jar", "proov-api.jar"]
