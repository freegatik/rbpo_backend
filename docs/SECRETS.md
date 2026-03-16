# Секреты и переменные

**GitHub.** Settings → Secrets and variables → Actions → Secrets. Repository secrets доступны воркфлоу; для разных окружений — Environments.

**Локально.** Дефолты в `application.properties`, профиль `local`. БД: `./scripts/setup-db.sh` (опционально `DB_NAME`, `DB_USER`, `DB_PASSWORD`). Запуск: `./run-local.sh`, порт 8081.

Переменные приложения: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (≥32 символа), `SERVER_PORT`, `JWT_ACCESS_EXPIRATION`, `JWT_REFRESH_EXPIRATION`, `SSL_*`. В local SSL выключен. Задавать в окружении или `application-local.properties`.

**ЭЦП.** Keystore для SHA256withRSA: `SIGNING_KEYSTORE_PATH`, `SIGNING_KEYSTORE_TYPE`, `SIGNING_KEYSTORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD` (если пусто — берётся пароль хранилища). По умолчанию `classpath:signing.jks`, создать: `./scripts/create-signing-keystore.sh`. В CI можно положить keystore в Base64 в секрет `SIGNING_KEYSTORE_BASE64` и при сборке декодировать в файл.

**CI.** Тесты на H2, секреты не обязательны. Для воркфлоу с реальной БД/подписью: в Repository secrets те же имена (`DB_URL`, `JWT_SECRET`, `SIGNING_*` при необходимости).
