#!/usr/bin/env python3
"""Offline static checks for project structure and the seeded recipe catalogue."""
from __future__ import annotations
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RECIPE_FILE = ROOT / "app/src/main/assets/recipes.json"
I18N_FILE = ROOT / "app/src/main/assets/recipe_i18n.json"
UI_COPY_FILE = ROOT / "app/src/main/java/it/studio44z/zing/ui/UiCopy.kt"
REQUIRED_PROJECT_FILES = [
    "settings.gradle.kts",
    "build.gradle.kts",
    "gradle.properties",
    "gradle/wrapper/gradle-wrapper.properties",
    "gradlew",
    "gradlew.bat",
    "app/build.gradle.kts",
    "app/src/main/AndroidManifest.xml",
    "app/src/main/java/it/studio44z/zing/data/AppDatabase.kt",
    "app/src/main/java/it/studio44z/zing/data/PantryRepository.kt",
    "app/src/main/java/it/studio44z/zing/ui/PantryApp.kt",
    "app/src/main/java/it/studio44z/zing/ui/CameraScannerScreen.kt",
    "app/src/main/java/it/studio44z/zing/domain/NutritionAdvisor.kt",
    "app/src/main/java/it/studio44z/zing/data/RecipeDiscoveryRepository.kt",
    "app/src/main/java/it/studio44z/zing/ui/RecipeLocalization.kt",
    "app/src/main/java/it/studio44z/zing/ui/UiCopy.kt",
    "app/src/main/java/it/studio44z/zing/domain/RecipeIngredientAliases.kt",
    "app/src/main/assets/recipe_i18n.json",
    "app/src/test/java/it/studio44z/zing/domain/QuantityMathTest.kt",
    "README.md",
]

def fail(message: str) -> None:
    print(f"ERRORE: {message}", file=sys.stderr)
    raise SystemExit(1)

for rel in REQUIRED_PROJECT_FILES:
    path = ROOT / rel
    if not path.is_file() or path.stat().st_size == 0:
        fail(f"file mancante o vuoto: {rel}")

try:
    recipes = json.loads(RECIPE_FILE.read_text(encoding="utf-8"))
except Exception as exc:
    fail(f"recipes.json non è JSON valido: {exc}")
if not isinstance(recipes, list):
    fail("recipes.json deve contenere una lista")
if len(recipes) < 50:
    fail(f"ricette iniziali insufficienti: {len(recipes)} (minimo 50)")

def normalized(value: str) -> str:
    return re.sub(r"\s+", " ", value.casefold()).strip()

names: set[str] = set()
content_signatures: set[tuple] = set()
for number, recipe in enumerate(recipes, start=1):
    if not isinstance(recipe, dict):
        fail(f"ricetta {number}: record non è un oggetto")
    for key in ("name", "description", "category", "prepMinutes", "cookMinutes", "servings", "ingredients", "instructions"):
        if key not in recipe:
            fail(f"ricetta {number}: campo richiesto mancante: {key}")
    name = recipe["name"]
    if not isinstance(name, str) or not name.strip():
        fail(f"ricetta {number}: nome vuoto")
    name_key = normalized(name)
    if name_key in names:
        fail(f"nome ricetta duplicato: {name}")
    names.add(name_key)
    if not isinstance(recipe["ingredients"], list) or not recipe["ingredients"]:
        fail(f"{name}: ingredienti assenti")
    for ingredient in recipe["ingredients"]:
        if not isinstance(ingredient, dict) or not isinstance(ingredient.get("name"), str) or not ingredient["name"].strip():
            fail(f"{name}: ingrediente senza nome valido")
        quantity = ingredient.get("quantity")
        if quantity is not None and (not isinstance(quantity, (int, float)) or quantity < 0):
            fail(f"{name}: quantità non valida per {ingredient['name']}")
        if quantity is not None and not str(ingredient.get("unit", "")).strip():
            fail(f"{name}: unità mancante per quantità numerica di {ingredient['name']}")
    if not isinstance(recipe["instructions"], str) or len(recipe["instructions"].strip()) < 25:
        fail(f"{name}: procedimento assente o troppo breve")
    try:
        servings = int(recipe["servings"])
        prep = int(recipe["prepMinutes"])
        cook = int(recipe["cookMinutes"])
    except (ValueError, TypeError):
        fail(f"{name}: porzioni/tempi non numerici")
    if servings <= 0 or prep < 0 or cook < 0:
        fail(f"{name}: porzioni o tempi fuori intervallo")
    ingredient_sig = tuple(sorted((normalized(i["name"]), i.get("quantity"), normalized(str(i.get("unit", "")))) for i in recipe["ingredients"]))
    procedure_sig = normalized(recipe["instructions"])
    signature = (ingredient_sig, procedure_sig)
    if signature in content_signatures:
        fail(f"contenuto ingredienti/procedimento duplicato: {name}")
    content_signatures.add(signature)

# Check that every bundled recipe has non-empty titles, descriptions and step-by-step translations
# in every secondary app language, and that all seed ingredient labels are covered.
try:
    i18n = json.loads(I18N_FILE.read_text(encoding="utf-8"))
except Exception as exc:
    fail(f"recipe_i18n.json non è JSON valido: {exc}")
expected_recipe_names = {recipe["name"] for recipe in recipes}
translations = i18n.get("translations", {})
required_languages = {"en", "es", "fr", "de", "pt"}
if set(translations) != required_languages:
    fail(f"lingue ricette errate: {sorted(translations)}")
for language in sorted(required_languages):
    table = translations.get(language, {})
    if set(table) != expected_recipe_names:
        missing = sorted(expected_recipe_names - set(table))
        extra = sorted(set(table) - expected_recipe_names)
        fail(f"{language}: ricette tradotte incomplete; mancanti={missing[:5]}, extra={extra[:5]}")
    for name, value in table.items():
        if not all(isinstance(value.get(key), str) and value[key].strip() for key in ("name", "description")):
            fail(f"{language}: nome/descrizione vuoti per {name}")
        steps = value.get("instructions")
        if not isinstance(steps, list) or not steps or not all(isinstance(step, str) and step.strip() for step in steps):
            fail(f"{language}: procedimento senza passaggi validi per {name}")
        if len(steps) < 2:
            fail(f"{language}: procedimento non suddiviso in passaggi per {name}")

expected_ingredients = {ingredient["name"] for recipe in recipes for ingredient in recipe["ingredients"]}
ingredient_translations = i18n.get("ingredients", {})
missing_ingredients = expected_ingredients - set(ingredient_translations)
if missing_ingredients:
    fail(f"ingredienti seed senza traduzione: {sorted(missing_ingredients)}")
for name in expected_ingredients:
    entry = ingredient_translations[name]
    for language in required_languages:
        if not isinstance(entry.get(language), str) or not entry[language].strip():
            fail(f"ingrediente {name}: traduzione {language} vuota")

# Verify that every literal passed directly to uiCopy() has a translation in all supported UI languages.
# UiCopy.kt contains a primary dictionary per language plus several supplemental nested dictionaries.
ui_source = UI_COPY_FILE.read_text(encoding="utf-8")
all_kotlin_source = "\n".join(path.read_text(encoding="utf-8") for path in (ROOT / "app/src/main/java/it/studio44z/zing").rglob("*.kt"))
ui_call_keys = set(re.findall(r'uiCopy\(\s*"((?:\\.|[^"\\])*)"', all_kotlin_source))

def balanced_parentheses(text: str, open_position: int) -> str:
    depth = 0
    in_string = False
    escaped = False
    for position in range(open_position, len(text)):
        char = text[position]
        if in_string:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == '"':
                in_string = False
            continue
        if char == '"':
            in_string = True
        elif char == "(":
            depth += 1
        elif char == ")":
            depth -= 1
            if depth == 0:
                return text[open_position + 1:position]
    return ""

ui_languages = {"en", "es", "fr", "de", "pt"}
ui_keys_by_language = {language: set() for language in ui_languages}
map_pattern = re.compile(r'private val (\w+)(?:\s*:\s*[^=]+)?\s*=\s*mapOf\s*\(')
for declaration in map_pattern.finditer(ui_source):
    map_name = declaration.group(1)
    map_body = balanced_parentheses(ui_source, declaration.end() - 1)
    if map_name in ui_keys_by_language:
        ui_keys_by_language[map_name].update(re.findall(r'"((?:\\.|[^"\\])*)"\s+to\s+"', map_body))
        continue
    for language in ui_languages:
        nested = re.search(r'"' + re.escape(language) + r'"\s+to\s+mapOf\s*\(', map_body)
        if nested:
            nested_body = balanced_parentheses(map_body, nested.end() - 1)
            ui_keys_by_language[language].update(re.findall(r'"((?:\\.|[^"\\])*)"\s+to\s+"', nested_body))

# Product/brand name is intentionally identical in every locale.
intentionally_untranslated = {"Zing Supporter Pro"}
for language in sorted(ui_languages):
    missing_ui = ui_call_keys - ui_keys_by_language[language] - intentionally_untranslated
    if missing_ui:
        fail(f"UI {language}: stringhe uiCopy non tradotte: {sorted(missing_ui)[:12]}")

print(f"OK: struttura progetto; {len(recipes)} ricette valide; traduzioni complete per {len(required_languages)} lingue aggiuntive; {len(expected_ingredients)} ingredienti tradotti; {len(ui_call_keys)} stringhe UI coperte nelle 5 lingue aggiuntive.")
