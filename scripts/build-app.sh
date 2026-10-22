#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "==> Building admin-web"
cd "$ROOT_DIR/admin-web"
pnpm install --frozen-lockfile
pnpm build

echo "==> Building backend jar"
cd "$ROOT_DIR/backend"
mvn clean package -DskipTests

JAR_FILE="$(find target -maxdepth 1 -type f -name 'mz-blog-*.jar' ! -name '*.original' | head -n 1)"
if [[ -z "$JAR_FILE" ]]; then
  echo "No application jar found under backend/target" >&2
  exit 1
fi

mkdir -p "$ROOT_DIR/deploy"
cp "$JAR_FILE" "$ROOT_DIR/deploy/app.jar"

echo "==> Ready: $ROOT_DIR/deploy/app.jar"
ls -lh "$ROOT_DIR/deploy/app.jar"
