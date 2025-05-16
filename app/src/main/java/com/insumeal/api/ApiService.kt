package com.insumeal.api

import com.insumeal.auth.LoginRequest
import com.insumeal.auth.LoginResponse

import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
