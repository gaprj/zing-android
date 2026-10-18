#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
OUT="${TMPDIR:-/tmp}/dispensa-smart-domain-check"
mkdir -p "$OUT"
kotlinc \
  "$ROOT/app/src/main/java/it/studio44z/zing/domain/RecipeIngredientAliases.kt" \
  "$ROOT/app/src/main/java/it/studio44z/zing/domain/QuantityMath.kt" \
  "$ROOT/app/src/main/java/it/studio44z/zing/domain/NutritionAdvisor.kt" \
  "$ROOT/app/src/main/java/it/studio44z/zing/data/FoodDateUtils.kt" \
  "$ROOT/app/src/main/java/it/studio44z/zing/data/RecipeIngredientRules.kt" \
  "$ROOT/tools/DomainSmokeTest.kt" \
  -include-runtime -d "$OUT/domain-smoke-tests.jar"
java -jar "$OUT/domain-smoke-tests.jar"
