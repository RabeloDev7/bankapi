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

# Cria o entrypoint diretamente no Dockerfile com LF garantido
RUN printf '#!/bin/sh\n\
set -e\n\
DB="${DATABASE_URL}"\n\
if echo "$DB" | grep -q "^postgresql://"; then\n\
  export SPRING_DATASOURCE_URL="jdbc:${DB}"\n\
elif echo "$DB" | grep -q "^postgres://"; then\n\
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB#postgres://}"\n\
elif echo "$DB" | grep -q "^jdbc:"; then\n\
  export SPRING_DATASOURCE_URL="${DB}"\n\
else\n\
  echo "ERRO: DATABASE_URL invalida: ${DB}"; exit 1\n\
fi\n\
echo "==> DB URL configurada com sucesso"\n\
exec java -Dspring.profiles.active=prod -Dserver.port="${PORT:-10000}" -jar app.jar\n\
' > /app/start.sh && chmod +x /app/start.sh

USER bankapi

EXPOSE 10000

ENTRYPOINT ["/bin/sh", "/app/start.sh"]
