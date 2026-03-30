package locapin.admin.utils

import io.ktor.server.plugins.BadRequestException

object Validators {
    fun requireNotBlank(value: String, field: String) {
        if (value.isBlank()) throw BadRequestException("$field is required")
    }
    fun validateLatLng(lat: Double, lng: Double) {
        if (lat !in -90.0..90.0) throw BadRequestException("Latitude must be between -90 and 90")
        if (lng !in -180.0..180.0) throw BadRequestException("Longitude must be between -180 and 180")
    }
    fun validatePrice(price: Double) {
        if (price < 0) throw BadRequestException("Price cannot be negative")
    }
    fun requireImage(contentType: String?) {
        if (contentType == null || !contentType.startsWith("image/")) throw BadRequestException("Invalid image file type")
    }
}
