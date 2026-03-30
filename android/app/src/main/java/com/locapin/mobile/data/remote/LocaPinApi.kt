package com.locapin.mobile.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LocaPinApi {
    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): ApiEnvelope<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiEnvelope<AuthResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ApiEnvelope<Unit>

    @GET("profile/me")
    suspend fun profile(): ApiEnvelope<UserDto>

    @GET("destinations")
    suspend fun destinations(
        @Query("q") query: String? = null,
        @Query("category") categoryId: String? = null,
        @Query("sort") sort: String? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null
    ): ApiEnvelope<List<DestinationDto>>

    @GET("destinations/{id}")
    suspend fun destinationDetail(@Path("id") id: String): ApiEnvelope<DestinationDto>

    @GET("categories")
    suspend fun categories(): ApiEnvelope<List<CategoryDto>>

    @GET("favorites")
    suspend fun favorites(): ApiEnvelope<List<DestinationDto>>

    @POST("favorites/{id}")
    suspend fun addFavorite(@Path("id") id: String): ApiEnvelope<Unit>

    @POST("favorites/{id}/remove")
    suspend fun removeFavorite(@Path("id") id: String): ApiEnvelope<Unit>
}
