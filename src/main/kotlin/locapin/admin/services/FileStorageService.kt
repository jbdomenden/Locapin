package locapin.admin.services

import io.ktor.http.content.PartData
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import java.io.File
import java.util.UUID

class FileStorageService(private val uploadDir: String) {
    suspend fun saveImages(call: ApplicationCall): List<String> {
        val result = mutableListOf<String>()
        call.receiveMultipart().forEachPart { part ->
            if (part is PartData.FileItem && part.name == "photos") {
                val ext = File(part.originalFileName ?: "image.jpg").extension.ifBlank { "jpg" }
                val targetName = "${UUID.randomUUID()}.$ext"
                val target = File(uploadDir, targetName)
                part.streamProvider().use { input -> target.outputStream().buffered().use { input.copyTo(it) } }
                result += "/uploads/$targetName"
            }
            part.dispose()
        }
        return result
    }
}
