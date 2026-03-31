package locapin.admin.services

import io.ktor.http.content.PartData
import io.ktor.http.content.streamProvider
import io.ktor.server.plugins.BadRequestException
import locapin.admin.utils.Validators
import java.io.File
import java.util.UUID

class FileStorageService(private val uploadRoot: String) {
    init { File(uploadRoot).mkdirs() }

    fun saveImage(part: PartData.FileItem): String {
        Validators.requireImage(part.contentType?.toString())
        val extension = part.originalFileName?.substringAfterLast('.', "jpg") ?: "jpg"
        val fileName = "${UUID.randomUUID()}.$extension"
        val target = File(uploadRoot, fileName)
        part.streamProvider().use { input -> target.outputStream().buffered().use { input.copyTo(it) } }
        if (!target.exists()) throw BadRequestException("Failed to save file")
        return "/uploads/$fileName"
    }
}
