plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "Locapin"

// Include Android app build so opening the repository root in Android Studio
// exposes mobile modules and run configurations alongside the Ktor backend.
includeBuild("android")
