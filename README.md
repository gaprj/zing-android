# Zing — Pantry & Recipes

Zing is a native Android app built with Kotlin, Jetpack Compose and Room. It helps you organize pantry items and expiry dates, discover recipes based on ingredients you have, and manage a shopping list.

## Features

- Pantry inventory with separate lots, quantities, locations, expiry dates, filters and sorting.
- Barcode scanning with CameraX and ML Kit, with optional product lookup through Open Food Facts.
- 64 built-in recipes available offline, with localized names, descriptions, ingredients and instructions in Italian, English, Spanish, French, German and Portuguese.
- Online recipe discovery through the public Wikibooks MediaWiki API. Results are heuristic and the online catalogue is not exhaustive.
- Ingredient availability checks, missing-ingredient comparison, recipe scaling and a shopping list.
- Local reminders, cooking timers, light/dark theme, language and unit preferences.
- Four-step animated first-run introduction, locally stored consecutive-day streak, and Play in-app update banner when an update is available.
- Optional one-time Zing Pro purchase through Google Play Billing; Pro activation is not included in manual JSON backups. Custom single-use Zing codes need the optional HTTPS backend under `server/pro_code_service`, which is source-only and not deployed.
- Optional nutrition profile and JSON export/import. Nutrition estimates are informational, not medical advice.
- Adaptive layouts for phones and tablets, including a navigation rail, adaptive grids and a two-pane recipe view on wider screens.

## Technology

- Kotlin, Jetpack Compose and Material 3
- Android Gradle Plugin 8.13.2, Gradle 8.13 and Kotlin 2.2.21
- `compileSdk = 36`, `targetSdk = 36`, `minSdk = 26`
- Room, DataStore, WorkManager, CameraX and ML Kit Barcode Scanning

## Build locally

1. Install Android Studio, Android SDK Platform 36 and the required SDK Build Tools. Use JDK 21 as the Gradle JDK (recommended for this project; avoid JDK 25 with Gradle 8.13).
2. Open the project directory containing `settings.gradle.kts` in Android Studio and allow Gradle to sync.
3. From the project root, run the relevant commands.

Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

Linux/macOS:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

The first build requires network access to download Gradle and dependencies. Build outputs and local IDE configuration are not included in the repository.

## External data sources

- **Open Food Facts** is used for optional barcode product lookup. Product data may be incomplete; consult the [Open Food Facts terms of use](https://world.openfoodfacts.org/terms-of-use) and API documentation before integrating or redistributing its data.
- **Wikibooks** is used for online recipe discovery. Results link to source pages; check the attribution and licensing information of individual pages before reusing their content.

## Source code license

No license is currently specified for this repository. Public availability on GitHub does not, by itself, grant permission to copy, modify or redistribute the source code.

## Release 1.2.0 test notes

See [`docs/RELEASE_1.2.0_TEST.md`](docs/RELEASE_1.2.0_TEST.md) for the release checklist, Pro-code backend configuration, device testing and closed-track upload steps. This project archive has not been built on the container environment; confirm the build and test tasks in Android Studio on Windows.
