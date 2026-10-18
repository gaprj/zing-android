# Zing — Pantry & Recipes

Zing is a native Android app built with Kotlin, Jetpack Compose and Room. It helps people organize pantry items and expiry dates, discover recipes from available ingredients, and manage a shopping list. The app is branded **Zing**; the proposed English Google Play listing title is **Zing: Pantry & Recipes**.

## Features

- Pantry inventory with separate lots, quantities, locations, expiry dates, filters and sorting.
- Barcode scanning with CameraX and ML Kit; product lookup through Open Food Facts when requested.
- 64 built-in recipes available offline, with localized names, descriptions, ingredients and instructions in Italian, English, Spanish, French, German and Portuguese.
- Recipe discovery using the public Wikibooks MediaWiki API; results are heuristic and the online catalogue is not exhaustive.
- Ingredient availability/missing-ingredient comparison, recipe scaling and a shopping list.
- Local reminders, cooking timers, light/dark theme, language and unit preferences.
- Optional nutrition profile and JSON export/import. Nutrition estimates are informational and are not medical advice.
- Adaptive layouts for phones and tablets, including a tablet navigation rail, adaptive grids and a two-pane recipe view on wide windows.

## Technology

- Kotlin, Jetpack Compose and Material 3
- Android Gradle Plugin 8.13.2, Gradle 8.13, Kotlin 2.2.21
- `compileSdk = 36`, `targetSdk = 36`, `minSdk = 26`
- Room, DataStore, WorkManager, CameraX, ML Kit Barcode Scanning and Google Play Billing

## Build locally

1. Install Android Studio, a compatible JDK 17, Android SDK Platform 36 and the SDK Build Tools through SDK Manager.
2. Open this directory (the one containing `settings.gradle.kts`) in Android Studio.
3. Let Gradle sync and download dependencies. Network access is required on first build.
4. On Windows, from the project root run:

   ```powershell
   .\gradlew.bat :app:assembleDebug
   .\gradlew.bat :app:testDebugUnitTest
   ```

   On Linux/macOS, run `./gradlew :app:assembleDebug` and `./gradlew :app:testDebugUnitTest`.

5. Run the app on a phone and a tablet, both portrait and landscape, before release. Check barcode lookup, online recipe search, notifications, timers, JSON backup/restore, language/theme changes and navigation after rotation.

The project maintainer reports that the current local Android Studio project builds with 0 errors and 0 warnings. This repository snapshot intentionally excludes generated build outputs and local IDE configuration; rebuild it from source. A clean release build and device testing should still be verified before Google Play submission.

## External services and licensing

- **Open Food Facts** is queried for barcode product information when the user requests a lookup. Data may be incomplete; follow its API terms, rate limits and database/content licensing.
- **Wikibooks** is queried for online recipe discovery when the user requests it. Recipe pages may have different page-specific notices; retain attribution, source links and license notices as implemented, and verify page-level terms. The online catalogue is community-maintained and not exhaustive.
- See [`docs/FONTI_E_TERZI.md`](docs/FONTI_E_TERZI.md) and [`docs/RECIPE_DISCOVERY_RELEASE.md`](docs/RECIPE_DISCOVERY_RELEASE.md).

## Before release

- Review [`docs/privacy-policy.md`](docs/privacy-policy.md) against the exact release build. It is a draft and must not be published without review.
- Complete Google Play Data safety, target audience, content rating and applicable health-related declarations truthfully.
- Google Play Billing is present in the code, but the one-time product `zing_pro_lifetime` must be configured and tested in Play Console before claiming that Pro is purchasable. The current project does not integrate Google Mobile Ads.
- The manifest currently has `android:allowBackup="true"`. Review Android cloud/device backup behavior and align it with the privacy policy and intended handling of local nutrition-profile data before public release.
- Confirm the public contact address embedded in service User-Agent strings is correct and intended to be public.
- No `LICENSE` file is included. Publicly hosting this repository does not grant permission to reuse the code; choose a license deliberately before describing it as open source.

## Repository hygiene

Build products, Gradle caches, Android Studio workspace state, local SDK paths, APK/AAB files, signing keys and personal backup exports must not be committed. The `.gitignore` excludes the main local/generated paths, but always review `git status --short` before committing.
