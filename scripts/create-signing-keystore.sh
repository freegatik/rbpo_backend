#!/usr/bin/env sh

set -e
OUT="${1:-src/main/resources/signing.jks}"
DIR="$(dirname "$OUT")"
mkdir -p "$DIR"

keytool -genkeypair \
  -alias app-signing \
  -keyalg RSA \
  -keysize 2048 \
  -sigalg SHA256withRSA \
  -keystore "$OUT" \
  -storetype JKS \
  -storepass changeit \
  -keypass changeit \
  -validity 3650 \
  -dname "CN=RBPO Backend Signing, OU=Dev, O=RBPO, L=City, ST=State, C=RU"

echo "Создан keystore: $OUT"
echo "Пароли: changeit. В проде задайте SIGNING_KEYSTORE_PASSWORD и SIGNING_KEY_PASSWORD."
