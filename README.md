# LocaPin Repository

This repository contains:
- **Admin backend** (`Ktor + Exposed + PostgreSQL`) at root.
- **End-user Android app** (`Jetpack Compose`) under `android/`.

## Android Studio run configuration fix
If you see `Unknown run configuration type KtorApplicationConfigurationType` while trying to run mobile code:
1. Close Android Studio.
2. Delete stale local config files under `.idea/runConfigurations/` and remove the unknown entry from `.idea/workspace.xml` (local machine only, not committed).
3. Re-open the project and use one of the checked-in run configs from `.run/`:
   - `Backend (Gradle)`
   - `Android App Assemble (Gradle)`
4. Fix SDK path error by setting one of the following:
   - `ANDROID_HOME` or `ANDROID_SDK_ROOT` environment variable, or
   - `android/local.properties` with `sdk.dir=C\\Users\\<you>\\AppData\\Local\\Android\\Sdk` on Windows.
5. For emulator launch/debug, open `android/` as a standalone project and run module `app`.

The root `settings.gradle.kts` includes the Android composite build **only when SDK path is configured**, so backend sync does not fail on non-Android environments.

---

## Backend (Admin) Setup
1. Copy `.env.example` to `.env` and fill values.
2. Start PostgreSQL and create `Locapin_db`.
3. Run: `./gradlew run`
4. Open: `http://localhost:9000/admin/login`

### Required `.env`
- `DB_URL=jdbc:postgresql://localhost:5432/Locapin_db`
- `DB_USER=postgres`
- `DB_PASSWORD=root`
- `ADMIN_INITIAL_NAME=...`
- `ADMIN_INITIAL_EMAIL=...`
- `ADMIN_INITIAL_PASSWORD=...`
- `SESSION_SECRET=...` (>=32 chars)
- `APP_ENV=development`
- `APP_PORT=9000`
- `FILE_UPLOAD_DIR=uploads`

### Bootstrap behavior
- Fails fast if required env values are missing.
- Creates first super admin from `.env` when `admin_users` is empty.
- Seeds San Juan City, one sample area, and one sample plan if those tables are empty.
