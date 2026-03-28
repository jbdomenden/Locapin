package locapin.admin.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.partialcontent.PartialContent
import io.ktor.server.http.content.staticFiles
import io.ktor.server.routing.routing
import java.io.File

fun Application.configureHttp() {
    install(DefaultHeaders)
    install(Compression)
    install(AutoHeadResponse)
    install(PartialContent)
    install(CachingHeaders)

    routing {
        staticFiles("/static", File("src/main/resources/static"))
        staticFiles("/uploads", File("uploads")) {
            enableAutoHeadResponse()
        }
    }
}
