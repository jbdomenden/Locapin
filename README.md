# LocaPin Repository

This repository contains:
- **Admin backend** (`Ktor + Exposed + PostgreSQL`) at root.
- **End-user Android app** (`Jetpack Compose`) under `android/`.

## Android Studio run configuration fix
If you see `Unknown run configuration type KtorApplicationConfigurationType` while trying to run mobile code:
1. Open the **repository root** and wait for Gradle sync to finish.
2. Select the Android run configuration **app** (not `EngineMain`).
3. If needed, open only `android/` as a standalone project.

The root `settings.gradle.kts` includes `includeBuild("android")` so Android modules are discoverable when opening the repository root.

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
