rootProject.name = "Locapin"

val androidDir = file("android")
val androidLocalProps = file("android/local.properties")
val hasAndroidHome = System.getenv("ANDROID_HOME")?.isNotBlank() == true ||
    System.getenv("ANDROID_SDK_ROOT")?.isNotBlank() == true
val hasSdkDirInLocalProps = androidLocalProps.exists() &&
    androidLocalProps.readText().lineSequence().any { it.trim().startsWith("sdk.dir=") }

// Only include the Android composite build when an SDK path is available.
// This prevents root Gradle sync from failing on backend-only environments.
if (androidDir.exists() && (hasAndroidHome || hasSdkDirInLocalProps)) {
    includeBuild("android")
} else {
    println("[Locapin] Android build not included in root sync. Set ANDROID_HOME/ANDROID_SDK_ROOT or android/local.properties (sdk.dir=...) to enable it.")
}
