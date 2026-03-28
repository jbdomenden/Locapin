package locapin.admin.utils

fun validateLatLng(lat: Double, lng: Double) {
    require(lat in -90.0..90.0) { "Latitude must be between -90 and 90." }
    require(lng in -180.0..180.0) { "Longitude must be between -180 and 180." }
}
