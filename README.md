# RBPO Backend

Spring Boot, JWT (access/refresh), роли USER/ADMIN/GUEST, PostgreSQL. Java 21, Spring Security, JPA, Gradle.

## Запуск

**Docker (БД проекта):**

```bash
docker compose up -d
export DB_URL=jdbc:postgresql://localhost:5434/rbpodb
export DB_USERNAME=rbpo
export DB_PASSWORD=rbpo
./run-local.sh
```

Контейнер `rbpo_backend_db`, порт 5434. psql: `docker exec -it rbpo_backend_db psql -U rbpo -d rbpodb`.

**Локальный PostgreSQL:**

```bash
chmod +x scripts/setup-db.sh && ./scripts/setup-db.sh
./run-local.sh
```

Порт 8081. Дефолт БД: `rbpodb` / `rbpo` / `rbpo`. Свои переменные — [SECRETS.md](docs/SECRETS.md).

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