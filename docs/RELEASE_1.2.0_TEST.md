# Zing 1.2.0 — third closed-testing release

## Included

- Tutorial slide/fade transition (220/180 ms) with interaction throttling.
- Play Billing flow for the one-time `zing_pro_lifetime` product, purchase querying on launch/resume, purchase acknowledgment, localized price, and activation-date metadata.
- Separate official Google Play promotional-code redemption and optional first-party single-use Zing codes.
- Pro-related preference/code/token/date fields filtered out from the manual JSON backup and ignored on import.
- en-US as the English UI spelling/date-format locale; Italian, Spanish, French, German and Portuguese retained.
- Google Play In-App Updates banner when Play reports an update for an eligible Play-installed app.
- Local calendar-day streak, never uploaded to a server and kept separate from the Pro entitlement.

## Important external configuration

- Version values in this source are `versionCode = 3` and `versionName = 1.2.0`. Before upload, verify that `versionCode = 2` / `1.1.0` really is the latest accepted version in the existing Play closed-testing track. Never reuse a version code already uploaded.
- This archive does not contain signing keys, Play credentials, `local.properties`, APK/AAB files, or user-specific Gradle settings.
- Real Play Billing availability depends on the product and its purchase option being active in Play Console, the payments profile being verified, and a build installed via Google Play testing. The product ID in the Android source is `zing_pro_lifetime`; keep it unchanged.
- First-party Zing codes require the included service to be deployed behind HTTPS. It is not deployed by this ZIP, and `ZING_PRO_REDEMPTION_BASE_URL` deliberately defaults to blank. Without a configured service, custom codes are safely refused and do not unlock Pro. Do not put private server credentials or a list of valid codes in Android source.
- The service's random installation ID is an installation-scoped identifier, not a verified user identity. Google Play purchase restore is the stronger device/account recovery path; email support for manual code-based transfers is shown in the UI.

## Configure Gradle JDK in Android Studio

Use **File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK** and select JDK 21. This avoids the observed `25.0.2` Gradle runtime error on some installations. Then wait for Gradle Sync to finish.

## Build and test from Android Studio menus

1. Open the directory containing `settings.gradle.kts`.
2. Open **View → Tool Windows → Build Variants** and select `debug` for `app`.
3. Choose **Build → Build Bundle(s) / APK(s) → Build APK(s)**. The expected APK is `app/build/outputs/apk/debug/app-debug.apk`.
4. Open **View → Tool Windows → Build** to confirm the result. Build failures are not considered passed tests.
5. If the terminal is usable after selecting JDK 21, run `./gradlew :app:assembleDebug` and `./gradlew :app:testDebugUnitTest`; on Windows PowerShell use `.\gradlew.bat :app:assembleDebug` and `.\gradlew.bat :app:testDebugUnitTest`.
6. The debug variant has the `.debug` application ID suffix and can coexist with the Play-installed app. Test update prompts and real billing against the Play-installed build, not just debug/sideloaded APKs.

## Manual QA checklist

- Tutorial: advance/back/skip/start, quick taps, second launch and reopening from Settings; check light/dark theme, landscape and tablet width.
- Pro: unavailable product shows a disabled purchase action; active product returns localized price; cancel/pending/failure do not grant Pro; completed Play purchase is queried again on resume and acknowledged; activation date is localized and is metadata only.
- Official Google Play promo: complete the Play redemption flow and resume Zing to refresh Billing.
- Custom codes: with an HTTPS server configured, test valid, invalid, already-used-on-another-installation, expired and offline cases. Without the server URL configured, no custom code is supposed to activate Pro.
- Backup: export JSON and verify it contains none of `supporterProUnlocked`, `zingProActivatedAt`, purchase tokens, custom codes or installation ID. Add such fields manually to a test JSON and confirm importing them cannot grant Pro.
- Streak: first open, repeated same-day opens, consecutive date, missed date, month boundary and year boundary.
- In-App Updates: test on a Play-installed build when Play reports a newer version; sideloaded/debug builds may not report an update and must not show a persistent error.
- Exercise all six languages and existing pantry, barcode, recipes, shopping list, nutrition profile, timer, backup/restore, UMP and optional AdMob banner.

## Upload to closed Alpha only

1. Back up the current source folder and test this ZIP in a separate directory.
2. After build, device QA and product/payment checks pass, choose **Build → Generate Signed Bundle / APK → Android App Bundle**.
3. Select the existing release keystore and alias. Do not create a new key. Create a signed **release AAB**.
4. In the existing Zing app in Google Play Console, select the same **Closed testing — Alpha** track and create a new release with that AAB. Do not choose Production and do not create a new app.
5. Complete Play Console checks, roll out to closed testing, then install the update through the existing tester opt-in link.
6. Confirm the installed release and have testers exercise the new version. Continue meeting the closed-test participation requirements before applying for production access.
