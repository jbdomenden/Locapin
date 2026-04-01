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
                ignoreIfMalformed = true
                ignoreIfMissing = true
            }
            fun read(key: String, required: Boolean = true, default: String? = null): String {
                return System.getenv(key)
                    ?: env[key]
                    ?: default
                    ?: if (required) error("Missing required environment variable: $key") else ""
            }
            val appEnv = read("APP_ENV", required = false, default = "development")
            val secret = System.getenv("SESSION_SECRET")
                ?: env["SESSION_SECRET"]
                ?: if (appEnv == "development") "dev-session-secret-change-me-32-chars" else error("Missing required environment variable: SESSION_SECRET")
            require(secret.length >= 32) { "SESSION_SECRET must be at least 32 characters." }

            return AppConfig(
                dbUrl = read("DB_URL"),
                dbUser = read("DB_USER"),
                dbPassword = read("DB_PASSWORD"),
                adminInitialName = read("ADMIN_INITIAL_NAME"),
                adminInitialEmail = read("ADMIN_INITIAL_EMAIL"),
                adminInitialPassword = read("ADMIN_INITIAL_PASSWORD"),
                sessionSecret = secret,
                appEnv = appEnv,
                appPort = read("APP_PORT", required = false, default = "9000").toInt(),
                fileUploadDir = read("FILE_UPLOAD_DIR", required = false, default = "uploads")
            )
        }
    }
}