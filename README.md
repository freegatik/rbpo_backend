# RBPO Backend

Spring Boot, JWT (access/refresh), роли USER/ADMIN/GUEST, PostgreSQL.

Java 21, Spring Security, JPA, Gradle.

**Запуск**

PostgreSQL должен быть запущен. Один раз:

```bash
chmod +x scripts/setup-db.sh && ./scripts/setup-db.sh
```

Потом:

```bash
./run-local.sh
```

Порт 8081. HTTPS в профиле `local` выключен.

Переменные БД по умолчанию: база `rbpodb`, юзер `rbpo`, пароль `rbpo`. Свои — `DB_NAME`, `DB_USER`, `DB_PASSWORD` перед вызовом `setup-db.sh`; для приложения — `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`. Остальное в [SECRETS.md](SECRETS.md).

**API**

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/api/auth/register` | Регистрация |
| POST | `/api/auth/login` | Логин, в ответе access + refresh |
| POST | `/api/auth/refresh` | Обновление пары токенов |
| GET | `/api/auth/me` | Текущий юзер, заголовок `Authorization: Bearer <token>` |

Тестовые юзеры после старта: `admin` / `Admin123!@#`, `testuser` / `Test123!@#`.

**Postman**

Import → `postman/rbpo-backend.postman_collection.json`. `baseUrl` = http://localhost:8081. После любого Login токен пишется в переменные коллекции, в Me подставляется сам.

**Сборка**

```bash
./gradlew test
./gradlew bootJar
```

В CI на push в `main`/`develop` — тесты, сборка, артефакт JAR.
