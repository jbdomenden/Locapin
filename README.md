# LocaPin Admin Website & Backend

This repository contains the **LocaPin admin website and backend only**.

## Stack
- Kotlin + **Ktor**
- **Exposed** ORM
- **PostgreSQL**
- Server-rendered/admin static frontend using **HTML, CSS, JavaScript**

## What this admin system manages
- Session-based admin authentication
- Super admin bootstrap from environment variables on first startup
- Admin dashboard
- San Juan City scoped content management
- CRUD for areas
- CRUD for attractions
- CRUD for attraction photos
- CRUD for subscription plans
- File uploads

## Prerequisites
- JDK 21
- PostgreSQL

## Environment setup
Create a `.env` file in the repository root.

Required values:

```env
DB_URL=jdbc:postgresql://localhost:5432/locapin
DB_USER=postgres
DB_PASSWORD=postgres

ADMIN_INITIAL_NAME=Super Admin
ADMIN_INITIAL_EMAIL=admin@locapin.com
ADMIN_INITIAL_PASSWORD=change-me-now

# Must be at least 32 characters (required outside development)
SESSION_SECRET=replace-with-a-long-random-secret-value
```

Optional values:

```env
APP_ENV=development
APP_PORT=9000
FILE_UPLOAD_DIR=uploads
```

## Run the admin backend
From the repository root:

```bash
./gradlew run
```

Default admin login URL:

- http://localhost:9000/admin/login

## Database notes
- Ensure the PostgreSQL database in `DB_URL` exists and credentials are valid.
- The app initializes schema and bootstrap data at startup, including the initial super admin account from `.env` if no admins exist.
- In `APP_ENV=development`, the app falls back to an internal dev session secret when `SESSION_SECRET` is not set; set `SESSION_SECRET` explicitly for shared/staging/production environments.
