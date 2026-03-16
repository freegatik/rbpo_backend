#!/usr/bin/env sh
DB_NAME="${DB_NAME:-rbpodb}"
DB_USER="${DB_USER:-rbpo}"
DB_PASSWORD="${DB_PASSWORD:-rbpo}"

if ! command -v psql >/dev/null 2>&1; then
  echo "Нужен psql (клиент PostgreSQL). Установите PostgreSQL."
  exit 1
fi

psql -d postgres -c "CREATE USER ${DB_USER} WITH PASSWORD '${DB_PASSWORD}';" 2>/dev/null || true
psql -d postgres -c "CREATE DATABASE ${DB_NAME} OWNER ${DB_USER};" 2>/dev/null || true
psql -d postgres -c "GRANT ALL PRIVILEGES ON DATABASE ${DB_NAME} TO ${DB_USER};"

echo "Готово: jdbc:postgresql://localhost:5432/${DB_NAME}, пользователь ${DB_USER}."
