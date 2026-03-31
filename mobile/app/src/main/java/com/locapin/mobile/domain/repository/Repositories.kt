package com.locapin.mobile.domain.repository

import com.locapin.mobile.core.common.LocaPinResult
import com.locapin.mobile.domain.model.Category
import com.locapin.mobile.domain.model.Destination
import com.locapin.mobile.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): LocaPinResult<Unit>
    suspend fun register(name: String, email: String, password: String): LocaPinResult<Unit>
    suspend fun forgotPassword(email: String): LocaPinResult<Unit>
    suspend fun logout()
    val authToken: Flow<String?>
}

interface DestinationRepository {
    suspend fun getDestinations(
        query: String? = null,
        categoryId: String? = null,
        sort: String? = null,
        page: Int = 1,
        lat: Double? = null,
        lng: Double? = null
    ): LocaPinResult<List<Destination>>

    suspend fun getDestinationDetail(id: String): LocaPinResult<Destination>
    suspend fun getCategories(): LocaPinResult<List<Category>>
    suspend fun getFavorites(): LocaPinResult<List<Destination>>
    suspend fun setFavorite(id: String, save: Boolean): LocaPinResult<Unit>
}

interface ProfileRepository {
    suspend fun getProfile(): LocaPinResult<User>
}
