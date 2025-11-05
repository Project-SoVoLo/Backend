# syntax=docker/dockerfile:1.6

# ---- Build stage ----
FROM --platform=$BUILDPLATFORM gradle:8.10.2-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar -x test

# ---- Run stage ----
FROM --platform=$TARGETPLATFORM eclipse-temurin:17-jre
WORKDIR /app
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
EXPOSE 8080
COPY --from=build /app/build/libs/*SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
