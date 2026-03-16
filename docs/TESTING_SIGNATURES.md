# Проверка сигнатур (задание 4)

URL: http://localhost:8081. Токен: после Login подставить в `Authorization: Bearer <token>`.

## Postman

1. Импорт `postman/rbpo-backend.postman_collection.json`, baseUrl = http://localhost:8081.
2. Сначала **Auth → Login as ADMIN** (токены сохраняются в переменные).
3. В папке Signatures: сначала **Create signature** — в ответе будет `id`, он попадёт в `signatureId`. Дальше можно вызывать Update, Delete, Get by IDs, Get history, Get audit.
4. Get full database и Get increment работают после любого логина (без сигнатур вернут []).

**Ошибка «value too long for type character varying(255)» при Create signature:** колонка `digital_signature_base64` в БД была создана с лимитом 255. Нужно расширить до text.

На хосте (приложение к localhost:5432, пользователь = тот же, что в приложении, обычно $USER):

```bash
psql -h localhost -p 5432 -U <пользователь> -d rbpodb -c "
ALTER TABLE signatures ALTER COLUMN digital_signature_base64 TYPE text;
ALTER TABLE signatures_history ALTER COLUMN digital_signature_base64 TYPE text;
"
```

Через Docker (приложение к контейнеру, например порт 5434):

```bash
docker exec -it rbpo_backend_db psql -U rbpo -d rbpodb -c "
ALTER TABLE signatures ALTER COLUMN digital_signature_base64 TYPE text;
ALTER TABLE signatures_history ALTER COLUMN digital_signature_base64 TYPE text;
"
```

После ALTER повторить Create signature.

## Операции

- **GET /api/signatures** — только ACTUAL.
- **GET /api/signatures/increment?since=2020-01-01T00:00:00Z** — все с updatedAt > since (включая DELETED). Без since — 400.
- **POST /api/signatures/by-ids** — тело `{"ids":["uuid",...]}`, возвращаются найденные.
- **POST /api/signatures** — создание (ADMIN), тело: threatName, firstBytesHex, remainderHashHex, remainderLength, fileType, offsetStart, offsetEnd. Ответ 201, в теле есть digitalSignatureBase64.
- **PUT /api/signatures/{id}** — обновление (ADMIN). В history — предыдущая версия, в audit — запись с fieldsChanged.
- **DELETE /api/signatures/{id}** — логическое удаление (ADMIN), 204. В таблице status = DELETED, в history и audit — записи.
- **GET /api/signatures/{id}/history** — список из signatures_history по signatureId.
- **GET /api/signatures/{id}/audit** — список из signatures_audit по signatureId.

Пример тела для создания:

```json
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

Чек-лист: все 8 операций дают ожидаемые коды; create/update пересчитывают подпись; delete только меняет status; update/delete пишут в history; create/update/delete — в audit; полная база без DELETED; инкремент с since включает DELETED; history и audit по id возвращают данные.
