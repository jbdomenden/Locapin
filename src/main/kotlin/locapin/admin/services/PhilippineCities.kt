package locapin.admin.services

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant

class PhilippineCities(
    private val sourceUrl: String = System.getenv("PH_CITIES_SOURCE_URL") ?: "https://psgc.gitlab.io/api/cities/"
) {
    @Volatile
    private var cachedCities: List<String> = emptyList()

    @Volatile
    private var lastSyncedAt: Instant? = null

    fun suggest(query: String?, limit: Int = 15): List<String> {
        val cities = loadCities()
        val normalized = query?.trim()?.lowercase().orEmpty()
        if (normalized.isBlank()) return cities.take(limit)
        return cities.asSequence()
            .filter { it.lowercase().contains(normalized) }
            .take(limit)
            .toList()
    }

    private fun loadCities(): List<String> {
        val now = Instant.now()
        val syncedAt = lastSyncedAt
        if (cachedCities.isNotEmpty() && syncedAt != null && Duration.between(syncedAt, now).toHours() < 24) {
            return cachedCities
        }

        synchronized(this) {
            val latestSyncedAt = lastSyncedAt
            if (cachedCities.isNotEmpty() && latestSyncedAt != null && Duration.between(latestSyncedAt, now).toHours() < 24) {
                return cachedCities
            }

            val refreshed = fetchCitiesFromSource()
            if (refreshed.isNotEmpty()) {
                cachedCities = refreshed
                lastSyncedAt = now
            } else if (cachedCities.isEmpty()) {
                cachedCities = fallbackCities.sorted()
                lastSyncedAt = now
            }
            return cachedCities
        }
    }

    private fun fetchCitiesFromSource(): List<String> = try {
        val request = HttpRequest.newBuilder(URI(sourceUrl))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) return emptyList()
        Json.parseToJsonElement(response.body()).jsonArray
            .mapNotNull { element -> element.jsonObject["name"]?.jsonPrimitive?.content?.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    } catch (_: Exception) {
        emptyList()
    }

    private val fallbackCities = listOf(
        "Cebu City", "City of Manila", "Davao City", "Makati City", "Mandaluyong City",
        "Marikina City", "Pasig City", "Passi City", "Quezon City", "Taguig City"
    )
}

