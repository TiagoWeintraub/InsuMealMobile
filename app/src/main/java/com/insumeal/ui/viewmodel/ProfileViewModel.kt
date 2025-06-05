package com.insumeal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insumeal.api.ProfileService
import com.insumeal.models.UserProfile
import com.insumeal.schemas.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileViewModel(private val userId: Int) : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val profileService: ProfileService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.0.170:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProfileService::class.java)
    }

    fun fetchUserProfile(authHeader: String, userId: String) {
        viewModelScope.launch {
            try {
                val response = profileService.getUserProfile(authHeader, userId.toInt())
                _userProfile.value = response.toModel()
            } catch (e: Exception) {
                _userProfile.value = null
            }
        }
    }
}
