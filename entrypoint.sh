#!/bin/sh
set -e

# O Render fornece DATABASE_URL como:
#   postgresql://user:pass@host/db   (formato interno)
#   postgres://user:pass@host/db     (formato legado)
#
# O Spring precisa de:
#   jdbc:postgresql://user:pass@host/db
#
# Este script faz a conversao e exporta SPRING_DATASOURCE_URL.

DB_URL="${DATABASE_URL}"

if echo "$DB_URL" | grep -q "^jdbc:"; then
  # Ja esta no formato jdbc: — usa direto
  export SPRING_DATASOURCE_URL="${DB_URL}"
elif echo "$DB_URL" | grep -q "^postgresql://"; then
  export SPRING_DATASOURCE_URL="jdbc:${DB_URL}"
elif echo "$DB_URL" | grep -q "^postgres://"; then
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_URL#postgres://}"
else
  echo "ERRO: DATABASE_URL nao reconhecida: ${DB_URL}"
  exit 1
fi

echo "==> Conectando em: $(echo $SPRING_DATASOURCE_URL | sed 's|//.*@|//<credenciais>@|')"

exec java \
  -Dspring.profiles.active=prod \
  -Dserver.port="${PORT:-10000}" \
  -jar app.jar
