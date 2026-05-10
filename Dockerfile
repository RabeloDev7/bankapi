# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copia o pom primeiro para aproveitar o cache de dependencias do Docker
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copia o codigo e compila (sem testes — rodam no CI)
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Usuario nao-root por seguranca
RUN addgroup -S bankapi && adduser -S bankapi -G bankapi
USER bankapi

COPY --from=build /app/target/bankapi-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
