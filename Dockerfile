# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S bankapi && adduser -S bankapi -G bankapi

COPY --from=build /app/target/bankapi-1.0.0.jar app.jar

USER bankapi

EXPOSE 10000

# SPRING_PROFILES_ACTIVE=prod e injetado pelo Render como variavel de ambiente
ENTRYPOINT ["java", "-jar", "app.jar"]
