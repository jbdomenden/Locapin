package locapin.admin

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import locapin.admin.config.AppConfig
import locapin.admin.db.DatabaseFactory
import locapin.admin.plugins.configureHTTP
import locapin.admin.plugins.configureRouting
import locapin.admin.plugins.configureSecurity
import locapin.admin.plugins.configureSerialization
import locapin.admin.plugins.configureStatusPages
import locapin.admin.services.BootstrapService

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val appConfig = AppConfig.load()
    environment.log.info("Starting LocaPin admin in ${appConfig.appEnv} on port ${appConfig.appPort}")
    monitor.subscribe(ApplicationStarted) {
        environment.log.info("LocaPin admin is responding at http://127.0.0.1:${appConfig.appPort}")
        environment.log.info(
            "Bootstrap superadmin credentials -> user: {} | pass: {}",
            appConfig.adminInitialEmail.lowercase(),
            appConfig.adminInitialPassword
        )
    }

    DatabaseFactory.init(appConfig)
    BootstrapService(appConfig).bootstrap()

    configureSerialization()
    configureHTTP()
    configureSecurity(appConfig)
    configureStatusPages()
    configureRouting(appConfig)
}
