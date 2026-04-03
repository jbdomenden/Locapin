# LocaPin Admin Website & Backend (San Juan City Only)

This repository is the **real Ktor + PostgreSQL-backed admin backend and website** for LocaPin content operations.

- It is scoped to **San Juan City only** (no Cities module in product UI).
- It serves authenticated admin pages and backend APIs used to manage map-driven attraction content.
- It is intended to be the content source for mobile integration (backend APIs + database), not a mock/demo app.

## Stack
- Kotlin + Ktor
- Exposed ORM
- PostgreSQL
- Static admin frontend (HTML/CSS/JavaScript)

## Active admin modules
- Login / session auth
- Dashboard
- Areas
- Attractions
- Attraction detail
- Photos
- Plans
- Users

## Prerequisites
- JDK 21
- PostgreSQL 14+

## Environment variables
Create a `.env` file in the repository root.

### Required
```env
DB_URL=jdbc:postgresql://localhost:5432/locapin
DB_USER=postgres
DB_PASSWORD=postgres

ADMIN_INITIAL_NAME=Super Admin
ADMIN_INITIAL_EMAIL=admin@locapin.com
ADMIN_INITIAL_PASSWORD=change-me-now

# Required outside development; min 32 chars
SESSION_SECRET=replace-with-a-long-random-secret-value
```

### Optional
```env
APP_ENV=development
APP_PORT=9000
FILE_UPLOAD_DIR=uploads
```

## Port behavior
- Default server port is **9000**.
- `APP_PORT` overrides the default at runtime.
- Config source: `src/main/resources/application.conf`.

## Fresh database setup
1. Create a new PostgreSQL database:
   ```sql
   CREATE DATABASE locapin;
   ```
2. Ensure `.env` `DB_URL/DB_USER/DB_PASSWORD` match your local DB.
3. Run the app once:
   ```bash
   ./gradlew run
   ```
4. On first run, schema tables are created and bootstrap seeds deterministic San Juan data.

## Bootstrap seed behavior (fresh DB)
Seeds run only when the relevant tables are empty.

- City scope seed: San Juan City
- Areas: Pinaglabanan, Greenhills, Little Baguio, West Crame, Addition Hills
- Attractions: 8 San Juan attractions (including Museo ng Katipunan, Pinaglabanan Shrine, Ronac Art Center, Fundacion Sanso, Art Sector Gallery, Greenhills Shopping Center, Greenhills Promenade, V-Mall)
- Plans: Explorer, Premium, Annual Premium
- End users + subscriptions for dashboard/testing counts
- Photo rows only if local image asset exists

### Development-only seeded admin test accounts
When admin users table initially contains only the env superadmin, additional deterministic test accounts are added:
- `admin.test@locapin.local` / `AdminTest123!`
- `editor.test@locapin.local` / `EditorTest123!`

These credentials are logged in development mode only.

## Run locally
From repository root:
```bash
./gradlew run
```

Admin login URL:
- http://localhost:9000/admin/login

## Local verification checklist
After first startup on a fresh DB, verify:

1. Login succeeds with env superadmin.
2. Dashboard shows non-zero San Juan seeded counts.
3. Areas page shows seeded San Juan areas.
4. Attractions page shows seeded attractions.
5. Attraction detail page loads full detail payload (name/description/knownFor/highlights/coords).
6. Photos page loads, lets you pick an attraction, and hits real upload/reorder/delete backend routes.
7. Plans page loads seeded plans.
8. Users page loads admin users.

## Notes
- No Cities module is present in active admin product scope.
- Session-cookie auth is required for `/admin/api/*` endpoints.
- Static assets are served under `/static` and uploaded files under `/uploads`.
