# LocaPin Monorepo

This repository is now organized into two top-level projects:

- `website/` — Admin website/backend (Ktor + Exposed + PostgreSQL), intended for IntelliJ IDEA.
- `mobile/` — Android mobile app (Jetpack Compose), intended for Android Studio.

## Quick start

### Website (IntelliJ)
1. Open `locapin/website` in IntelliJ IDEA.
2. Configure `.env` values for database and app settings.
3. Run:
   ```bash
   cd website
   ./gradlew run
   ```

### Mobile (Android Studio)
1. Open `locapin/mobile` in Android Studio.
2. Set Android SDK path via `ANDROID_HOME`/`ANDROID_SDK_ROOT` or `mobile/local.properties`.
3. Add required API and Maps keys in Gradle/local properties.
4. Run module `app` on an emulator or device.
