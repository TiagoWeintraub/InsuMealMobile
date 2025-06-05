package com.insumeal.repository

import com.insumeal.api.ProfileService
import com.insumeal.models.UserProfile
import com.insumeal.schemas.toModel

class ProfileRepository(private val profileService: ProfileService) {
    suspend fun getUserProfile(authHeader: String, userId: String): UserProfile? {
        return try {
            profileService.getUserProfile(authHeader, userId.toInt()).toModel()
        } catch (e: Exception) {
            null
        }
    }
}
