# Build Stage
FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app

# copy common library
COPY connect4-common/ /connect4-common/

# copy statistics project
COPY connect4-statistics/gradlew ./
COPY connect4-statistics/gradle/ ./gradle/
COPY connect4-statistics/build.gradle connect4-statistics/settings.gradle ./
COPY connect4-statistics/src ./src

# correct line-endings
RUN apk add --no-cache dos2unix && \
    dos2unix ./gradlew && \
    chmod +x ./gradlew

# execute build
RUN ./gradlew clean build --no-daemon -x test

# Runtime Stage
FROM openjdk:21-ea-jdk-slim
ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT [ "java", "-jar", "/app.jar" ]
EXPOSE 9005