package com.insumeal.models

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val username: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String,
)