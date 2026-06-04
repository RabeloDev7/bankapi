# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copia pom.xml separado para aproveitar cache de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Compila o projeto
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Cria usuario nao-root (root precisa criar o usuario antes de trocar)
RUN addgroup -S bankapi && adduser -S bankapi -G bankapi

# Copia JAR e script de entrada
COPY --from=build /app/target/bankapi-1.0.0.jar app.jar
COPY entrypoint.sh entrypoint.sh

# Permissao de execucao antes de trocar de usuario
RUN chmod +x entrypoint.sh

# Troca para usuario nao-root
USER bankapi

# Render usa a porta 10000 por padrao
EXPOSE 10000

ENTRYPOINT ["sh", "entrypoint.sh"]
