# Build Stage
FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app

# copy common library
COPY connect4-common/ /connect4-common/

# copy friendlist project
COPY connect4-friendlist/gradlew ./
COPY connect4-friendlist/gradle/ ./gradle/
COPY connect4-friendlist/build.gradle connect4-friendlist/settings.gradle ./
COPY connect4-friendlist/src ./src

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
EXPOSE 9006