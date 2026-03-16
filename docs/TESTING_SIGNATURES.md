# Проверка модуля антивирусных сигнатур (задание 4)

После запуска приложения (и логина под `admin` / `Admin123!@#`) можно проверить все 8 операций.

**Базовый URL:** `http://localhost:8081`  
**Токен:** после `POST /api/auth/login` с `{"username":"admin","password":"Admin123!@#"}` подставьте `accessToken` в заголовок `Authorization: Bearer <token>`.

---

## Как пользоваться Postman

1. Импортируйте коллекцию `postman/rbpo-backend.postman_collection.json`. Переменная `baseUrl` по умолчанию `http://localhost:8081`.
2. **Обязательно** выполните **Auth → Login as ADMIN** — в ответе придут токены, они автоматически сохранятся в переменные коллекции (`accessToken`, `refreshToken`).
3. Папка **Signatures**: сначала выполните **Create signature** — в ответе будет созданная сигнатура с полем `id`. Этот `id` автоматически сохранится в переменную `signatureId`. После этого можно вызывать **Update signature**, **Delete signature**, **Get by IDs**, **Get history**, **Get audit** (они подставляют `{{signatureId}}` в URL).
4. **Get full database** и **Get increment** можно вызывать сразу после логина (подставляется тот же `accessToken`). Пока нет ни одной сигнатуры, они вернут пустой массив `[]` — это нормально.

Если при **Create signature** была ошибка «value too long for type character varying(255)», в БД уже создана колонка старой длины. Если при выполнении ALTER в Docker вы видите `relation "signatures" does not exist` — приложение, скорее всего, подключается к **другой** БД (по умолчанию `localhost:5432`, а не контейнер). Выполните ALTER в той БД, куда реально подключается приложение (см. `DB_URL` в конфиге). Ниже команды для случая, когда таблицы уже есть и колонка была varchar(255).

**На хосте (если приложение использует БД на localhost:5432 — значение по умолчанию):**

```bash
psql -h localhost -p 5432 -U <ваш_пользователь_БД> -d rbpodb -c "
ALTER TABLE signatures ALTER COLUMN digital_signature_base64 TYPE text;
ALTER TABLE signatures_history ALTER COLUMN digital_signature_base64 TYPE text;
"
```

Пользователь должен совпадать с тем, под которым подключается приложение: по умолчанию это `$USER` (логин в ОС), либо значение `DB_USERNAME`, если задано. Пароль запросит psql при необходимости.

**Через Docker (если приложение подключается к контейнеру, например DB_URL=jdbc:postgresql://localhost:5434/rbpodb):**

```bash
docker exec -it rbpo_backend_db psql -U rbpo -d rbpodb -c "
ALTER TABLE signatures ALTER COLUMN digital_signature_base64 TYPE text;
ALTER TABLE signatures_history ALTER COLUMN digital_signature_base64 TYPE text;
"
```

Или зайти в psql и выполнить по одной команде:

```bash
docker exec -it rbpo_backend_db psql -U rbpo -d rbpodb
```

В интерактивной консоли psql:

```sql
ALTER TABLE signatures ALTER COLUMN digital_signature_base64 TYPE text;
ALTER TABLE signatures_history ALTER COLUMN digital_signature_base64 TYPE text;
\q
```

После этого заново выполните **Create signature**.

---

## 1. Полная база (только ACTUAL)

```http
GET /api/signatures
Authorization: Bearer <token>
```

Ожидание: список только с `status: "ACTUAL"`. Записей со статусом `DELETED` нет.

---

## 2. Инкремент (since обязателен)

```http
GET /api/signatures/increment?since=2020-01-01T00:00:00Z
Authorization: Bearer <token>
```

Ожидание: все записи, у которых `updatedAt > since` (включая с `status: "DELETED"`). Без параметра `since` — 400.

---

## 3. По списку UUID

```http
POST /api/signatures/by-ids
Content-Type: application/json
Authorization: Bearer <token>

{"ids": ["<uuid-существующей-сигнатуры>"]}
```

Ожидание: массив найденных сигнатур (только переданные id).

---

## 4. Создание (ADMIN)

```http
POST /api/signatures
Content-Type: application/json
Authorization: Bearer <token>

{
  "threatName": "Test.Malware",
  "firstBytesHex": "4d5a",
  "remainderHashHex": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
  "remainderLength": 0,
  "fileType": "exe",
  "offsetStart": 0,
  "offsetEnd": 2
}
```

Ожидание: 201 Created, в теле есть `digitalSignatureBase64` (подпись пересчитана при создании).

---

## 5. Обновление (ADMIN)

Измените любое поле и вызовите:

```http
PUT /api/signatures/<id>
Content-Type: application/json
Authorization: Bearer <token>

{ ... те же поля с новыми значениями ... }
```

Ожидание: 200 OK, новая `digitalSignatureBase64`. В таблице `signatures_history` появляется предыдущая версия. В `signatures_audit` — запись об обновлении с `fieldsChanged`.

---

## 6. Логическое удаление (ADMIN)

```http
DELETE /api/signatures/<id>
Authorization: Bearer <token>
```

Ожидание: 204 No Content. Запись в `signatures` остаётся, но `status` = `DELETED`. В истории — версия до удаления. В аудите — запись об удалении.

---

## 7. История по signatureId (ADMIN)

```http
GET /api/signatures/<id>/history
Authorization: Bearer <token>
```

Ожидание: список записей из `signatures_history` для этого `signatureId`, от новых к старым.

---

## 8. Аудит по signatureId (ADMIN)

```http
GET /api/signatures/<id>/audit
Authorization: Bearer <token>
```

Ожидание: список записей из `signatures_audit` для этого `signatureId`, от новых к старым (create/update/delete).

---

## Чек-лист

- [ ] Все 8 операций отвечают указанными кодами и форматом.
- [ ] Create и Update: в ответе есть `digitalSignatureBase64`, при изменении полей подпись меняется.
- [ ] Delete: запись не удаляется из БД, только `status` = DELETED.
- [ ] Update и Delete: в `signatures_history` появляется запись перед изменением.
- [ ] Create, Update, Delete: в `signatures_audit` есть соответствующие записи.
- [ ] GET полной базы: только записи с `status: ACTUAL`.
- [ ] GET инкремента с `since`: все записи с `updatedAt > since`, включая DELETED.
- [ ] GET .../history и GET .../audit по существующему `signatureId` возвращают данные.
