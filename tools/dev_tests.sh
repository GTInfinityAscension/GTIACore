#!/usr/bin/env bash
# EN: runData + GameTests with GTNA; retries a task once when the dev classpath comes up without GTNA/AE2 (flaky).
# PT: runData + GameTests com o GTNA; repete a tarefa uma vez quando o classpath de dev sobe sem GTNA/AE2 (instável).
# ZH: 带 GTNA 运行 runData 与 GameTest；开发类路径缺少 GTNA/AE2（偶发）时重试一次。
set -uo pipefail
P="$HOME/MineProjects"
cd "$(dirname "$0")/.."
F=(-PgtiaDevOptionalDependencies=true -PgtnaDevJar="$P/GregTech-Nexus-Addon/build/devlibs/gtna-0.6.0.jar" --offline)
run() {
  for attempt in 1 2 3; do
    ./gradlew "$1" "${F[@]}" > "build/$1.log" 2>&1
    grep -q "requires §6\(gtna\|ae2\)" run/logs/latest.log || return 0
    echo "retry $1 ($attempt): dev classpath without GTNA/AE2"
  done
}
run runData
run runGameTestServer
grep -hE "required tests|failed!" "build/runGameTestServer.log" | cut -c1-400
