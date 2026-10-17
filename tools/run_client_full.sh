#!/usr/bin/env bash
# EN: runClient with GTNA (local main), AE2, ModernSpace and ModernManipulator for playtesting.
# PT: runClient com GTNA (main local), AE2, ModernSpace e ModernManipulator para teste no jogo.
# ZH: 带 GTNA（本地 main）、AE2、ModernSpace 与 ModernManipulator 的 runClient，用于实机测试。
set -euo pipefail
P="$HOME/MineProjects"
cd "$(dirname "$0")/.."
exec ./gradlew runClient -PgtiaDevOptionalDependencies=true \
  -PgtnaDevJar="$P/GregTech-Nexus-Addon/build/devlibs/gtna-0.6.1.jar" \
  -PmodernSpaceJar="$P/PersonalSpace/build/devlibs/modernspace-0.3.1.jar" \
  -PmodernManipulatorJar="$P/ModernManipulator/build/devlibs/modern-manipulator-0.2.0.jar" "$@"
