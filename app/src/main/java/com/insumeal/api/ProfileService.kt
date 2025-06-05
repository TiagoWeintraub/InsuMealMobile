package com.insumeal.api

import com.insumeal.schemas.UserProfileSchema
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ProfileService {
    @GET("/users/get_by_id/{user_id}")
    suspend fun getUserProfile(
        @Header("Authorization") authHeader: String,
        @Path("user_id") userId: Int
    ): UserProfileSchema
}
