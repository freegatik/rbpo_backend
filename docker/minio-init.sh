#!/bin/sh
set -e
ROOT_USER="${MINIO_ROOT_USER:-minioadmin}"
ROOT_PASS="${MINIO_ROOT_PASSWORD:-minioadminsecret}"
APP_USER="${MINIO_APP_USER:-rbpoapp}"
APP_SECRET="${MINIO_APP_SECRET:-rbpoappsecret}"
BUCKET="${MINIO_BUCKET_NAME:-rbpo-signature-files}"
POLICY_PATH="${MINIO_POLICY_FILE:-/policy/minio-init-policy.json}"
HOST="${MINIO_HOST:-minio}"

mc alias set local "http://${HOST}:9000" "$ROOT_USER" "$ROOT_PASS"
mc mb "local/${BUCKET}" --ignore-existing
mc anonymous set none "local/${BUCKET}"

mc admin user add "local/${APP_USER}" "$APP_SECRET" 2>/dev/null || true

mc admin policy remove local rbpo-signature-files-policy 2>/dev/null || true
mc admin policy create local rbpo-signature-files-policy "$POLICY_PATH"
mc admin policy attach local rbpo-signature-files-policy "user=${APP_USER}"

echo "MinIO init ok: bucket=${BUCKET}, app user=${APP_USER}"
