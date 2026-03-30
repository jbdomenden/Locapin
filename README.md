# LocaPin Admin (Ktor + Kotlin)

Admin panel for managing LocaPin tourism content for San Juan City.

## Features
- Session-based admin auth with BCrypt password checks.
- Bootstrap first super admin from `.env` only.
- Dashboard with counts and latest attraction summaries.
- CRUD-style management for cities, areas, attractions, photos, and subscription plans.
- JSON endpoints for async admin interactions.
- Server-rendered UI (plain HTML templates in resources) + responsive branded CSS + vanilla JS.

## Setup
1. Copy environment file:
   ```bash
   cp .env.example .env
   ```
2. Update admin bootstrap secrets in `.env`.
3. Ensure PostgreSQL exists:
   - Database: `Locapin_db`
   - User/password matching `.env`
4. Run app:
   ```bash
   ./gradlew run
   ```
5. Open `http://localhost:9000/admin/login`.

## Required environment variables
- `DB_URL=jdbc:postgresql://localhost:5432/Locapin_db`
- `DB_USER=postgres`
- `DB_PASSWORD=root`
- `ADMIN_INITIAL_NAME`
- `ADMIN_INITIAL_EMAIL`
- `ADMIN_INITIAL_PASSWORD`
- `SESSION_SECRET`
- `APP_ENV=development`
- `APP_PORT=9000`
- `FILE_UPLOAD_DIR=uploads`

## Bootstrap behavior
- On startup, schema is created if missing.
- If no row exists in `admin_users`, app creates one super admin using `.env` values.
- If required env vars are missing, app fails fast before serving requests.

## Seed data
- City: San Juan City (premium-enabled).
- Areas: Greenhills, Little Baguio, West Crame.
- Plans: Monthly Premium, Yearly Premium.

## Extension points
- Add role-based authorization checks in `auth/`.
- Add migration tooling (Flyway/Liquibase) for production rollouts.
- Replace local upload storage with object storage by swapping `FileStorageService`.
- Add pagination/search service wrappers per module for larger datasets.
