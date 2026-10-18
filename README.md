# Zing — Pantry & Recipes

Zing is a native Android app for managing pantry items, tracking expiry dates, discovering recipes, and organizing a shopping list. It is built with Kotlin, Jetpack Compose, and Room.

## Features

- Pantry inventory with item lots, quantities, locations, expiry dates, filters, and sorting.
- Barcode scanning with CameraX and ML Kit, with optional product lookup through Open Food Facts.
- 64 built-in recipes available offline, with localized names, descriptions, ingredients, and instructions in Italian, English, Spanish, French, German, and Portuguese.
- Online recipe discovery through the Wikibooks MediaWiki API. Results are heuristic and the online catalogue is not exhaustive.
- Ingredient availability checks, recipe scaling, and a shopping list.
- Local reminders, cooking timers, light/dark theme, language and unit preferences.
- Optional nutrition profile and JSON export/import. Nutrition estimates are informational and are not medical advice.
- Adaptive layouts for phones and tablets, including a navigation rail, adaptive grids, and a two-pane recipe view on wide windows.

## Technology

- Kotlin, Jetpack Compose, Material 3
- Android Gradle Plugin 8.13.2, Gradle 8.13, Kotlin 2.2.21
- `compileSdk = 36`, `targetSdk = 36`, `minSdk = 26`
- Room, DataStore, WorkManager, CameraX, ML Kit Barcode Scanning, Google Play Billing

## Build locally

1. Install Android Studio, JDK 17 or JDK 21 (both compatible with this project toolchain), Android SDK Platform 36, and the required Build Tools.
2. Open this repository's root directory in Android Studio—the directory containing `settings.gradle.kts`.
3. Let Gradle sync and download dependencies. Network access is required on the first build.
4. Build and run the app from Android Studio, or use the included Gradle bootstrap scripts from the project root:

   **Windows PowerShell**

   ```powershell
   .\gradlew.bat :app:assembleDebug
   .\gradlew.bat :app:testDebugUnitTest
   ```

   **Linux/macOS**

   ```sh
   ./gradlew :app:assembleDebug
   ./gradlew :app:testDebugUnitTest
   ```

The maintainer has verified the debug APK build on a local Android Studio setup and reports that all 8 unit tests pass. Build outputs and local IDE settings are intentionally not committed; they are regenerated locally.

## External data sources

- [Open Food Facts](https://world.openfoodfacts.org/) provides product information for barcode lookups. Data may be incomplete; its API terms, rate limits, and applicable database/content licences apply.
- [Wikibooks](https://www.wikibooks.org/) is queried for online recipe discovery. The catalogue is community-maintained and not exhaustive. Recipe pages may have page-specific licence and attribution requirements; source attribution should be preserved where applicable.

## Licensing

No license is granted for general reuse, modification, or redistribution of this project's code. Public visibility on GitHub does not mean the project is open source. All rights remain with the copyright holder, except for rights granted by applicable law or GitHub's platform terms. Third-party libraries, data, and content remain subject to their respective licences and terms.
