package com.locapin.mobile.data.repository

import com.locapin.mobile.core.common.LocaPinResult
import com.locapin.mobile.core.datastore.UserPreferencesDataStore
import com.locapin.mobile.data.local.InMemoryCache
import com.locapin.mobile.data.remote.AuthRequest
import com.locapin.mobile.data.remote.ForgotPasswordRequest
import com.locapin.mobile.data.remote.LocaPinApi
import com.locapin.mobile.data.remote.RegisterRequest
import com.locapin.mobile.domain.model.Category
import com.locapin.mobile.domain.model.Destination
import com.locapin.mobile.domain.model.User
import com.locapin.mobile.domain.repository.AuthRepository
import com.locapin.mobile.domain.repository.DestinationRepository
import com.locapin.mobile.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: LocaPinApi,
    private val prefs: UserPreferencesDataStore
) : AuthRepository {
    override val authToken: Flow<String?> = prefs.authToken

    override suspend fun login(email: String, password: String): LocaPinResult<Unit> = runCatching {
        api.login(AuthRequest(email, password)).data?.token
    }.fold(
        onSuccess = { token ->
            if (token.isNullOrBlank()) LocaPinResult.Error("Invalid login response")
            else {
                prefs.setAuthToken(token)
                LocaPinResult.Success(Unit)
            }
        },
        onFailure = { LocaPinResult.Error(it.message ?: "Login failed") }
    )

    override suspend fun register(name: String, email: String, password: String): LocaPinResult<Unit> =
        runCatching { api.register(RegisterRequest(name, email, password)).data?.token }.fold(
            onSuccess = { token ->
                if (token.isNullOrBlank()) LocaPinResult.Error("Invalid registration response")
                else {
                    prefs.setAuthToken(token)
                    LocaPinResult.Success(Unit)
                }
            },
            onFailure = { LocaPinResult.Error(it.message ?: "Registration failed") }
        )

    override suspend fun forgotPassword(email: String): LocaPinResult<Unit> = runCatching {
        api.forgotPassword(ForgotPasswordRequest(email))
    }.fold(
        onSuccess = { LocaPinResult.Success(Unit) },
        onFailure = { LocaPinResult.Error(it.message ?: "Request failed") }
    )

    override suspend fun logout() = prefs.clearSession()
}

@Singleton
class DestinationRepositoryImpl @Inject constructor(
    private val api: LocaPinApi,
    private val cache: InMemoryCache
) : DestinationRepository {
    override suspend fun getDestinations(
        query: String?,
        categoryId: String?,
        sort: String?,
        page: Int,
        lat: Double?,
        lng: Double?
    ): LocaPinResult<List<Destination>> = runCatching {
        api.destinations(query, categoryId, sort, page = page, lat = lat, lng = lng).data
            ?.map { it.toDomain() }
            ?: emptyList()
    }.fold(
        onSuccess = {
            cache.lastDestinations = it
            LocaPinResult.Success(it)
        },
        onFailure = {
            if (cache.lastDestinations.isNotEmpty()) LocaPinResult.Success(cache.lastDestinations)
            else LocaPinResult.Error(it.message ?: "Unable to load destinations")
        }
    )

    override suspend fun getDestinationDetail(id: String): LocaPinResult<Destination> = runCatching {
        api.destinationDetail(id).data?.toDomain()
    }.fold(
        onSuccess = { it?.let(LocaPinResult::Success) ?: LocaPinResult.Error("Destination not found") },
        onFailure = { LocaPinResult.Error(it.message ?: "Failed to load destination") }
    )

    override suspend fun getCategories(): LocaPinResult<List<Category>> = runCatching {
        api.categories().data?.map { Category(it.id, it.name) } ?: emptyList()
    }.fold(
        onSuccess = {
            cache.lastCategories = it
            LocaPinResult.Success(it)
        },
        onFailure = {
            if (cache.lastCategories.isNotEmpty()) LocaPinResult.Success(cache.lastCategories)
            else LocaPinResult.Error(it.message ?: "Unable to load categories")
        }
    )

    override suspend fun getFavorites(): LocaPinResult<List<Destination>> = runCatching {
        api.favorites().data?.map { it.toDomain() } ?: emptyList()
    }.fold(
        onSuccess = { LocaPinResult.Success(it) },
        onFailure = { LocaPinResult.Error(it.message ?: "Unable to load favorites") }
    )

    override suspend fun setFavorite(id: String, save: Boolean): LocaPinResult<Unit> = runCatching {
        if (save) api.addFavorite(id) else api.removeFavorite(id)
    }.fold(
        onSuccess = { LocaPinResult.Success(Unit) },
        onFailure = { LocaPinResult.Error(it.message ?: "Unable to update favorite") }
    )
}

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: LocaPinApi
) : ProfileRepository {
    override suspend fun getProfile(): LocaPinResult<User> = runCatching {
        api.profile().data?.toDomain()
    }.fold(
        onSuccess = { it?.let(LocaPinResult::Success) ?: LocaPinResult.Error("Missing profile") },
        onFailure = { LocaPinResult.Error(it.message ?: "Unable to load profile") }
    )
}
