# RBPO Backend

Бэкенд на Spring Boot: аутентификация (JWT access/refresh), ролевая авторизация, HTTPS, PostgreSQL.

## Стек

- Java 21, Spring Boot 3.3
- Spring Security, JWT (jjwt)
- Spring Data JPA, PostgreSQL
- Gradle

## Требования

- Java 21+
- PostgreSQL 12+ (для запуска приложения)

## Запуск

```bash
# БД и учётные данные (при необходимости)
export DB_URL=jdbc:postgresql://localhost:5432/rbpodb
export DB_USERNAME=rbpo
export DB_PASSWORD=rbpo

# Без HTTPS (если нет keystore)
export SSL_ENABLED=false

./gradlew bootRun
```

Порт по умолчанию: **8081**.

## Конфигурация

Основные переменные окружения и секреты для локального запуска и CI описаны в [SECRETS.md](SECRETS.md).

## API

### Аутентификация

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/api/auth/register` | Регистрация |
| POST | `/api/auth/login` | Логин (access + refresh токены) |
| POST | `/api/auth/refresh` | Обновление токенов |
| GET  | `/api/auth/me` | Текущий пользователь (требуется `Authorization: Bearer <accessToken>`) |

Роли: **USER**, **ADMIN**, **GUEST**. Публичные маршруты: register, login, refresh; остальные — по правилам в `SecurityConfig`.

### Тестовые пользователи

При первом запуске создаются:

| Роль  | Username   | Password      |
|-------|------------|---------------|
| ADMIN | `admin`    | `Admin123!@#` |
| USER  | `testuser` | `Test123!@#`  |

## Сборка и тесты

```bash
./gradlew test      # тесты
./gradlew build     # сборка
./gradlew bootJar   # JAR
```

CI (GitHub Actions): при push/PR в `main` и `develop` выполняются тесты и сборка, артефакт JAR сохраняется в Actions.
