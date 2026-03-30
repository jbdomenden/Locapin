# LocaPin Mobile (Android)

Production-ready Jetpack Compose Android client for LocaPin end users.

## Stack
- Kotlin + Jetpack Compose + Material 3
- Hilt DI
- Retrofit + Kotlin serialization
- DataStore for onboarding/session/recent searches
- Google Maps Compose + Fused Location Provider
- Single-activity architecture with Navigation Compose

## Setup
1. Open `/workspace/Locapin/android` in Android Studio (latest stable).
2. Create `~/.gradle/gradle.properties` (or project `local.properties`) with:
   ```properties
   LOCAPIN_API_BASE_URL=https://<your-ktor-host>/
   MAPS_API_KEY=<your_google_maps_key>
   ```
3. Ensure backend API endpoints align with `LocaPinApi` routes in:
   - `app/src/main/java/com/locapin/mobile/data/remote/LocaPinApi.kt`
4. Sync and run on Pixel 8 emulator.

## Backend route assumptions
The current client assumes:
- `POST /auth/login`
- `POST /auth/register`
- `POST /auth/forgot-password`
- `GET /profile/me`
- `GET /destinations`, `GET /destinations/{id}`
- `GET /categories`
- `GET /favorites`, `POST /favorites/{id}`, `POST /favorites/{id}/remove`

Adjust only `LocaPinApi` DTOs/routes if backend shape differs; the repository/domain/UI layers are isolated from DTO contracts.

## Branding assets
- App uses `ic_locapin_logo.xml` for launcher/splash/auth branding hook points.
- Raw provided brand asset copied as `res/raw/locapin_logo.svg` for future richer branding usage.
