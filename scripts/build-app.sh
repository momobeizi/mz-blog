#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "==> Building admin-web"
cd "$ROOT_DIR/admin-web"

export COREPACK_ENABLE_DOWNLOAD_PROMPT=0
export CI=true
if command -v corepack >/dev/null 2>&1; then
  PNPM=(corepack pnpm)
else
  PNPM=(npx --yes pnpm@10.15.0)
fi

INSTALL_ARGS=(--frozen-lockfile)
if [[ -f node_modules/.modules.yaml ]] \
  && ! grep -q '^packageManager: pnpm@10.15.0$' node_modules/.modules.yaml; then
  INSTALL_ARGS+=(--force)
fi

"${PNPM[@]}" install "${INSTALL_ARGS[@]}"
"${PNPM[@]}" build

echo "==> Building backend jar"
cd "$ROOT_DIR/backend"
mvn clean package -DskipTests

JAR_FILE="$(find target -maxdepth 1 -type f -name 'mz-blog-*.jar' ! -name '*.original' | head -n 1)"
if [[ -z "$JAR_FILE" ]]; then
  echo "No application jar found under backend/target" >&2
  exit 1
fi

mkdir -p "$ROOT_DIR/deploy"
cp "$JAR_FILE" "$ROOT_DIR/deploy/mz-blog.jar"

echo "==> Ready: $ROOT_DIR/deploy/mz-blog.jar"
ls -lh "$ROOT_DIR/deploy/mz-blog.jar"
