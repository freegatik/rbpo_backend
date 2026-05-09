# RBPO Backend

Spring Boot, JWT (access/refresh), роли USER/ADMIN/GUEST, PostgreSQL. Java 21, Spring Security, JPA, Gradle.

## Запуск

**К какой БД подключается приложение**

| Режим | Условие | Подключение |
|--------|---------|-------------|
| Дефолт в `application.properties` | Нет переопределения `DB_*` | `localhost:5434/rbpodb`, `rbpo` / `rbpo` (как в Docker Compose) |
| Свой PostgreSQL | задать `DB_URL` / при необходимости `DB_USERNAME` / `DB_PASSWORD` | например `localhost:5432`, свой пользователь |

**Docker: Postgres + веб-интерфейс (pgAdmin)**

```bash
docker compose up -d
./scripts/run-local.sh
```

Без `export` приложение уже нацелено на эту БД. При необходимости переопредели `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

- Браузер: **http://localhost:5051** — pgAdmin. Логин: `admin@example.com`, пароль: `admin` (или задай `PGADMIN_EMAIL` / `PGADMIN_PASSWORD` перед `up`; адрес должен быть «настоящим» для pgAdmin, не `*.local`).
- В pgAdmin: Add New Server → Connection: Host **db**, Port **5432**, DB **rbpodb**, User **rbpo**, Password **rbpo** (`db` — имя сервиса в compose).

Контейнер БД: `rbpo_backend_db`, с хоста порт **5434**. psql: `docker exec -it rbpo_backend_db psql -U rbpo -d rbpodb`.

**MinIO (файлы сигнатур, задание 6):** после `docker compose up -d` доступны API **9000**, консоль **9001** ([образ](https://hub.docker.com/r/minio/minio)). Сервис `minio-init` создаёт приватный bucket **`rbpo-signature-files`** и пользователя **`rbpoapp`** / **`rbpoappsecret`** (не root). Для приложения на хосте:

```bash
export MINIO_ENDPOINT=http://127.0.0.1:9000
export MINIO_ACCESS_KEY=rbpoapp
export MINIO_SECRET_KEY=rbpoappsecret
```

Root (`MINIO_ROOT_*`) только для администрирования MinIO; Spring использует ключи **`MINIO_ACCESS_KEY`** / **`MINIO_SECRET_KEY`**.

**Локальный PostgreSQL (не Docker):** подними БД и задай подключение, например:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/rbpodb
export DB_USERNAME=myuser
export DB_PASSWORD=mypass
./scripts/run-local.sh
```

Порт 8081. Свои переменные — [SECRETS.md](docs/SECRETS.md).

**ЭЦП.** Тикет в activate/check/renew подписывается SHA256withRSA (канонический JSON по RFC 8785). Keystore по умолчанию: `classpath:signing.jks` (пароль `changeit`). Свой keystore: `./scripts/create-signing-keystore.sh`.

## API


| Метод  | Путь                                       | Описание                                          |
| ------ | ------------------------------------------ | ------------------------------------------------- |
| POST   | `/api/auth/register`                       | Регистрация                                       |
| POST   | `/api/auth/login`                          | Логин (access + refresh)                          |
| POST   | `/api/auth/refresh`                        | Обновление токенов                                |
| GET    | `/api/auth/me`                             | Текущий пользователь                              |
| POST   | `/api/licenses`                            | Создание лицензии (ADMIN)                         |
| POST   | `/api/licenses/activate`                   | Активация (activationKey, deviceMac, deviceName?) |
| POST   | `/api/licenses/check`                      | Проверка (deviceMac, productId)                   |
| POST   | `/api/licenses/renew`                      | Продление (activationKey)                         |
| GET    | `/api/signatures`                          | Полная база сигнатур (USER/ADMIN)                 |
| GET    | `/api/signatures/increment?since=ISO-8601` | Инкремент (USER/ADMIN)                            |
| POST   | `/api/signatures/by-ids`                   | По списку UUID (USER/ADMIN)                       |
| POST   | `/api/signatures`                          | Создание сигнатуры (ADMIN)                        |
| PUT    | `/api/signatures/{id}`                     | Обновление (ADMIN)                                |
| DELETE | `/api/signatures/{id}`                     | Логическое удаление (ADMIN)                       |
| GET    | `/api/signatures/{id}/history`             | История (ADMIN)                                   |
| GET    | `/api/signatures/{id}/audit`               | Аудит (ADMIN)                                     |
| GET    | `/api/binary/signatures/full`              | Бинарная полная выгрузка (`multipart/mixed`)      |
| GET    | `/api/binary/signatures/increment?since=`  | Бинарный инкремент (`since` ISO-8601 обязателен)  |
| POST   | `/api/binary/signatures/by-ids`            | Бинарная выдача по телу `{ "ids": [uuid…] }`      |
| POST   | `/api/signatures/files/upload`             | Загрузка файла → расчёт полей + MinIO (ADMIN)    |
| POST   | `/api/signatures/files/presigned-urls`    | Presigned GET по `{ "ids": [...] }` (ADMIN)       |

Формат потоков описан в [multipart.md](https://github.com/MatorinFedor/RBPO_2025_demo/blob/master/files/multipart.md): части `manifest.bin` и `data.bin`, числа Big-endian. Префиксы magic `MF-` / `DB-` задаются суффиксом фамилии: `rbpo.binary-format.student-surname` или `RBPO_BINARY_STUDENT_SURNAME` (по умолчанию `RBPO`). Подпись манифеста — SHA256withRSA по байтам неподписанного манифеста (`SignatureService.signBytes`).

Тестовые пользователи: `admin` / `Admin123!@#`, `testuser` / `Test123!@#`.

**Postman:** импорт `postman/rbpo-backend.postman_collection.json`, baseUrl = [http://localhost:8081](http://localhost:8081). После Login токен попадает в переменные коллекции.

**Сборка:** `./gradlew test` и `./gradlew bootJar`. CI: тесты и JAR на push в main/develop.

---

## ЗИоВПО. 

По [методичке](https://github.com/MatorinFedor/RBPO_2025_demo/blob/master/files/licenses.md).

**БД:** users, product, license_type, license, device, device_license, license_history. Лицензия связана с продуктом, типом и владельцем; активация — через device_license.

**Создание лицензии.** POST /api/licenses. Проверки: продукт, тип, владелец (404 при отсутствии). Генерация кода, запись в license и license_history (CREATED). Ответ 201.

**Активация.** POST /api/licenses/activate. Лицензия по коду. **Первая активация** в коде: `user_id` ещё null (после создания лицензии админом). Тогда выставляются user, first_activation_date, ending_date и device_license; в истории — «Первая активация». Иначе — только доп. устройство, лимит device_count (409 при превышении). Ответ 200, TicketResponse.

**Проверка.** POST /api/licenses/check. Устройство по MAC; активная лицензия по device, user, product (не заблокирована, ending_date >= now). Ответ 200, TicketResponse.

**Продление.** POST /api/licenses/renew. Нужны активированная лицензия (`user_id` и `first_activation_date` заданы). Если `ending_date` null — выставляется срок «с сейчас» на default_duration (без окна в 7 дней). Если `ending_date` задана — как в методичке: не раньше чем за 7 дней до истечения (или уже просрочена). Ответ 200, TicketResponse.

**Тикет и ЭЦП.** Ticket: serverDate, ttlSeconds, activationDate, expiryDate, userId, deviceId, blocked. TicketResponse = тикет + подпись. Подпись: канонический JSON (RFC 8785) → SHA256withRSA → Base64. Проверка на клиенте: та же канонизация, верификация публичным ключом из сертификата.