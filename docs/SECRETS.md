# Секреты и переменные

**GitHub.** Settings → Secrets and variables → Actions → Secrets → New repository secret. Имя и значение. Repository secrets видны всем воркфлоу; для разных окружений — Environments.

**Локально.** Дефолты в `application.properties` и профиль `local` — ничего вешать в окружение не надо. БД: `./scripts/setup-db.sh` (или свои `DB_NAME`, `DB_USER`, `DB_PASSWORD`). Запуск: `./run-local.sh`. Порт 8081.

Свои значения — в окружении или в `application-local.properties`. Переменные: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (длина от 32 символов), `SERVER_PORT`, `JWT_ACCESS_EXPIRATION`, `JWT_REFRESH_EXPIRATION`, `SSL_ENABLED`, `SSL_KEYSTORE`, `SSL_KEYSTORE_PASSWORD`, `SSL_KEY_ALIAS`, `SSL_KEY_PASSWORD`. В профиле local SSL выключен.

**Модуль ЭЦП (подпись тикета лицензии).** Хранилище ключей для SHA256withRSA:
- `SIGNING_KEYSTORE_PATH` — путь к keystore (например `file:./signing.jks` или `classpath:signing.jks`).
- `SIGNING_KEYSTORE_TYPE` — тип (JKS или PKCS12).
- `SIGNING_KEYSTORE_PASSWORD` — пароль хранилища.
- `SIGNING_KEY_ALIAS` — алиас ключа (по умолчанию `app-signing`).
- `SIGNING_KEY_PASSWORD` — пароль ключа (если не задан, используется пароль хранилища).

Для GitHub Actions: можно хранить keystore в Base64 в секрете `SIGNING_KEYSTORE_BASE64`, при сборке/деплое декодировать в файл и задать `SIGNING_KEYSTORE_PATH=file:./decoded.jks`. Локально и в CI по умолчанию используется `classpath:signing.jks` (создаётся скриптом `./scripts/create-signing-keystore.sh`).

**CI.** Секреты в репо не обязательны: тесты на H2, сборка без подстановки. Если понадобится передавать в воркфлоу — те же имена в Repository secrets: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, при необходимости — `SIGNING_KEYSTORE_BASE64`, `SIGNING_KEYSTORE_PASSWORD`, `SIGNING_KEY_PASSWORD`, `SIGNING_KEY_ALIAS`.

**Из music-streaming.** Можно скопировать те же секреты сюда вручную. JWT_SECRET лучше новый. Для HTTPS в CI (здесь не используется): `SSL_KEYSTORE_BASE64`, `SSL_KEYSTORE_PASSWORD`, `SSL_KEY_ALIAS`, `SSL_KEY_PASSWORD`, `STUDENT_ID`.
