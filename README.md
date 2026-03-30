# LocaPin Admin (Ktor + Exposed + PostgreSQL)

A full rewrite of the admin-side implementation for LocaPin.

## Stack
- Kotlin + Ktor
- Exposed DSL
- PostgreSQL
- Static HTML/CSS/Vanilla JS frontend

## Setup
1. Copy `.env.example` to `.env` and fill values.
2. Start PostgreSQL and create `Locapin_db`.
3. Run: `./gradlew run`
4. Open: `http://localhost:9000/admin/login`

## Required `.env`
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

## Bootstrap behavior
- Fails fast if required env values are missing.
- Creates first super admin from `.env` when `admin_users` is empty.
- Seeds San Juan City, one sample area, and one sample plan if those tables are empty.

## Future extension points
- Add role-based authorization checks per route.
- Add audit logs and admin activity timeline.
- Add richer analytics and exports.
- Add pagination and server-side sorting endpoints.
