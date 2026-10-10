#!/usr/bin/env bash
set -euo pipefail

core_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)"

if [[ "$core_dir" != *:* ]]; then
    cd "$core_dir"
    exec ./gradlew "$@"
fi

if ! command -v bwrap >/dev/null 2>&1; then
    echo "O caminho do GTIACore contém ':'; instale bubblewrap (bwrap) ou mova o projeto para um caminho sem ':'." >&2
    exit 1
fi

mount_dir="$(mktemp -d /tmp/gtiacore-gradle.XXXXXX)"
trap 'rmdir "$mount_dir"' EXIT

bwrap --bind / / --dev-bind /dev /dev --proc /proc \
    --bind "$core_dir" "$mount_dir" --chdir "$mount_dir" \
    ./gradlew "$@"
