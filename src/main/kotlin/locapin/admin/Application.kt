package locapin.admin

import io.ktor.server.application.Application
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

    DatabaseFactory.init(appConfig)
    BootstrapService(appConfig).bootstrap()

    configureSerialization()
    configureHTTP()
    configureSecurity(appConfig)
    configureStatusPages()
    configureRouting(appConfig)
}
