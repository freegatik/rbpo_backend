# RBPO Backend

Spring Boot, JWT (access/refresh), роли USER/ADMIN/GUEST, PostgreSQL.

Java 21, Spring Security, JPA, Gradle.

**Запуск**

**Вариант A: своя БД проекта в Docker**

```bash
docker compose up -d
export DB_URL=jdbc:postgresql://localhost:5434/rbpodb
export DB_USERNAME=rbpo
export DB_PASSWORD=rbpo
./run-local.sh
```

Контейнер `rbpo_backend_db`, порт **5434** (не конфликтует с другим Postgres на 5433). Данные в volume `rbpo_backend_pgdata`. Вход в psql: `docker exec -it rbpo_backend_db psql -U rbpo -d rbpodb`. Для проверки продления (Renew): приложение и psql должны использовать одну и ту же БД — создай лицензию через API при подключении к 5434, затем в psql в этом же контейнере выполни `UPDATE license SET ending_date = NOW() + INTERVAL '3 days' WHERE code = 'КОД';` и вызови Renew с заголовком `Authorization: Bearer {{accessToken}}` (сначала Login as USER).

**Вариант B: внешний PostgreSQL**

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

**Подпись тикета (модуль ЭЦП).** Ответы activate/check/renew содержат тикет и ЭЦП (SHA256withRSA по каноническому JSON, RFC 8785). По умолчанию используется keystore из `classpath:signing.jks` (пароль `changeit`). Создать свой: `./scripts/create-signing-keystore.sh` (по умолчанию перезаписывает `src/main/resources/signing.jks`).

**API**

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/api/auth/register` | Регистрация |
| POST | `/api/auth/login` | Логин, в ответе access + refresh |
| POST | `/api/auth/refresh` | Обновление пары токенов |
| GET | `/api/auth/me` | Текущий юзер, заголовок `Authorization: Bearer <token>` |
| POST | `/api/licenses` | Создание лицензии (только ADMIN), тело: productId, typeId, ownerId, deviceCount?, description? |
| POST | `/api/licenses/activate` | Активация лицензии, тело: activationKey, deviceMac, deviceName?; ответ: TicketResponse |
| POST | `/api/licenses/check` | Проверка лицензии, тело: deviceMac, productId; ответ: TicketResponse |
| POST | `/api/licenses/renew` | Продление лицензии, тело: activationKey; ответ: TicketResponse |

Тестовые юзеры после старта: `admin` / `Admin123!@#`, `testuser` / `Test123!@#`.

**Postman**

Import → `postman/rbpo-backend.postman_collection.json`. `baseUrl` = http://localhost:8081. После любого Login токен пишется в переменные коллекции, в Me подставляется сам.

**Сборка**

```bash
./gradlew test
./gradlew bootJar
```

В CI на push в `main`/`develop` — тесты, сборка, артефакт JAR.

---

## ЗИоВПО. Описание схем подсистемы лицензирования

Текстовое описание приведено по [методическому пособию](https://github.com/MatorinFedor/RBPO_2025_demo/blob/master/files/licenses.md).

### ER-диаграмма (структура БД)

- **users** — пользователи системы (администратор, пользователь). Поля: id, имя, хеш пароля, email, роль, флаги состояния учётки.
- **product** — справочник лицензируемых продуктов. Поля: id, name, is_blocked.
- **license_type** — типы лицензий (TRIAL, MONTH, YEAR, CORPORATE и т.д.). Поля: id, name, default_duration_in_days, description.
- **license** — лицензия как право использования. Поля: id, code (активационный ключ), product_id, type_id, owner_id (владелец), user_id (кто активировал, заполняется при первой активации), first_activation_date, ending_date, blocked, device_count, description.
- **device** — устройства пользователя. Поля: id, name, mac_address, user_id.
- **device_license** — связь лицензия–устройство (факт активации на устройстве). Поля: id, license_id, device_id, activation_date.
- **license_history** — журнал событий по лицензии (аудит). Поля: id, license_id, user_id, status (CREATED, ACTIVATED, RENEWED), change_date, description.

Связи: одна лицензия — один продукт и один тип; у лицензии один owner и один активировавший user; устройство принадлежит одному user; device_license связывает лицензии и устройства многие-ко-многим.

### Диаграмма последовательности: создание лицензии

Администратор вызывает `POST /api/licenses`. Контроллер передаёт запрос в сервис с идентификатором администратора. Сервис проверяет существование и активность продукта (productId) — иначе 404; затем тип лицензии (typeId) — иначе 404; затем владельца (ownerId) — иначе 404. Затем в одной транзакции: генерируется код лицензии, сохраняется запись в `license` (owner задан, user = null), в `license_history` добавляется запись со статусом CREATED и user = adminId. Ответ 201 и данные созданной лицензии.

### Диаграмма последовательности: активация лицензии

Пользователь вызывает `POST /api/licenses/activate` (activationKey, deviceMac, deviceName). Сервис загружает лицензию по коду; если не найдена — 404. Если лицензия уже активирована на другого пользователя — 403. Устройство ищется по MAC; если не найдено — создаётся и привязывается к пользователю. При первой активации: лицензии задаётся user, first_activation_date, ending_date = now + default_duration_in_days типа; создаётся запись device_license; в историю пишется ACTIVATED. При повторной активации проверяется лимит устройств (число записей device_license для данной лицензии); при достижении лимита — 409; иначе создаётся device_license и запись в истории. Ответ 200 и TicketResponse (тикет + ЭЦП).

### Диаграмма последовательности: проверка лицензии

Пользователь вызывает `POST /api/licenses/check` (deviceMac, productId). Сервис находит устройство по MAC; если не найдено — 404. Ищется активная лицензия: по устройству, user_id и product_id, не заблокирована, ending_date >= текущей даты, есть связь в device_license. Если не найдена — 404. Ответ 200 и TicketResponse.

### Диаграмма последовательности: продление лицензии

Пользователь вызывает `POST /api/licenses/renew` (activationKey). Сервис находит лицензию по коду; если не найдена — 404. Проверяется, что лицензия принадлежит текущему пользователю; иначе 403. Проверяется возможность продления (например, лицензия неактивна или до истечения не более 7 дней); иначе 409. К ending_date прибавляется default_duration_in_days типа лицензии; лицензия сохраняется; в историю пишется RENEWED. Ответ 200 и TicketResponse.

### Тикет (Ticket) и TicketResponse

Тикет передаётся клиенту и содержит: текущую дату сервера, время жизни тикета (TTL), дату активации лицензии, дату истечения лицензии, идентификатор пользователя, идентификатор устройства, флаг блокировки лицензии. TicketResponse содержит тикет и ЭЦП: тикет приводится к каноническому JSON (RFC 8785), подписывается алгоритмом SHA256withRSA приватным ключом из keystore, подпись возвращается в Base64. Проверка на клиенте: та же канонизация полей тикета, UTF-8 байты, верификация через `Signature.initVerify(publicKey)` и публичный ключ из сертификата.
