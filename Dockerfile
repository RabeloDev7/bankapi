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
USER bankapi

COPY --from=build /app/target/bankapi-1.0.0.jar app.jar

# Render injeta a porta via variável PORT (padrão 10000)
EXPOSE 10000

ENTRYPOINT ["java", \
  "-Dspring.profiles.active=prod", \
  "-Dserver.port=${PORT:-10000}", \
  "-jar", "app.jar"]
