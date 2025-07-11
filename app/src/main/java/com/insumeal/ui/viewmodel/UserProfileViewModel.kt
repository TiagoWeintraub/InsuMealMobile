package com.insumeal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insumeal.api.ProfileService
import com.insumeal.api.RetrofitClient
import com.insumeal.models.UserProfile
import com.insumeal.schemas.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserProfileViewModel : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile
    private val profileService: ProfileService by lazy {
        RetrofitClient.retrofit.create(ProfileService::class.java)
    }

    fun loadUserProfile(authHeader: String, userId: String) {
        viewModelScope.launch {
            try {
                val response = profileService.getUserProfile(authHeader, userId.toInt())
                _userProfile.value = response.toModel()
            } catch (e: Exception) {
                _userProfile.value = null
            }
        }
    }

    fun clearProfile() {
        _userProfile.value = null
    }

    fun setUserProfile(profile: UserProfile) {
        _userProfile.value = profile
    }
}
