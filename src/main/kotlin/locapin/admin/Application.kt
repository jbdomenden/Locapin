package locapin.admin

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import locapin.admin.config.AppConfig
import locapin.admin.db.DatabaseFactory
import locapin.admin.plugins.configureHttp
import locapin.admin.plugins.configureMonitoring
import locapin.admin.plugins.configureSecurity
import locapin.admin.plugins.configureSerialization
import locapin.admin.plugins.configureStatusPages
import locapin.admin.routes.configureRoutes

fun main() {
    val config = AppConfig.load()
    embeddedServer(Netty, port = config.appPort, host = "0.0.0.0") {
        module(config)
    }.start(wait = true)
}

fun Application.module(config: AppConfig = AppConfig.load()) {
    configureMonitoring()
    configureHttp()
    configureSerialization()
    configureStatusPages()
    configureSecurity(config)

    DatabaseFactory.init(config)
    DatabaseFactory.seed(config)

    configureRoutes(config)
}
