package locapin.admin.config

import io.github.cdimascio.dotenv.dotenv

data class AppConfig(
    val dbUrl: String,
    val dbUser: String,
    val dbPassword: String,
    val adminInitialName: String,
    val adminInitialEmail: String,
    val adminInitialPassword: String,
    val sessionSecret: String,
    val appEnv: String,
    val appPort: Int,
    val fileUploadDir: String
) {
    companion object {
        fun load(): AppConfig {
            val env = dotenv {
                ignoreIfMalformed = false
                ignoreIfMissing = true
            }
            fun required(key: String): String =
                env[key] ?: System.getenv(key) ?: error("Missing required environment variable: $key")

            return AppConfig(
                dbUrl = required("DB_URL"),
                dbUser = required("DB_USER"),
                dbPassword = required("DB_PASSWORD"),
                adminInitialName = required("ADMIN_INITIAL_NAME"),
                adminInitialEmail = required("ADMIN_INITIAL_EMAIL"),
                adminInitialPassword = required("ADMIN_INITIAL_PASSWORD"),
                sessionSecret = required("SESSION_SECRET"),
                appEnv = required("APP_ENV"),
                appPort = required("APP_PORT").toInt(),
                fileUploadDir = required("FILE_UPLOAD_DIR")
            )
        }
    }
}
