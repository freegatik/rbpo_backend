#!/usr/bin/env sh

set -e
KEYSTORE="${1:-src/main/resources/signing.jks}"
ALIAS="${2:-app-signing}"
STOREPASS="${3:-changeit}"

if [ ! -f "$KEYSTORE" ]; then
  echo "Создайте keystore: ./scripts/create-signing-keystore.sh"
  exit 1
fi

keytool -exportcert -rfc -alias "$ALIAS" -keystore "$KEYSTORE" -storepass "$STOREPASS"
