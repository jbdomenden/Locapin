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
3. Ensure Android SDK path is configured (required):
   - via `ANDROID_HOME` / `ANDROID_SDK_ROOT`, or
   - via `android/local.properties`:
   ```properties
   sdk.dir=C\Users\<you>\AppData\Local\Android\Sdk
   ```
4. Ensure backend API endpoints align with `LocaPinApi` routes in:

   - `app/src/main/java/com/locapin/mobile/data/remote/LocaPinApi.kt`
5. Sync and run on Pixel 8 emulator.

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


## Segmented Map integration note
- The segmented San Juan experience is wired to the existing bottom navigation **Map** entry (`Routes.Map`).
- Temporary area/attraction seeds are isolated in `data/local/SanJuanSeedDataSource.kt` and automatically replaced by backend data when `GET /map/areas` and `GET /map/attractions` are available.

## Troubleshooting Gradle zip errors
If Android Studio/Gradle fails during configuration with:

`java.util.zip.ZipException: zip END header not found`

one of the cached Gradle artifacts is usually truncated/corrupted. From the repository root, run:

```bash
./gradlew --stop
rm -rf ~/.gradle/caches
rm -rf ~/.gradle/wrapper/dists
./gradlew -p android :app:help --refresh-dependencies
```

Then re-sync the Android project in Android Studio.
