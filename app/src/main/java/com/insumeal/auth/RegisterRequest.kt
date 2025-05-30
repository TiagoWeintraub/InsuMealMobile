package com.insumeal.auth
import com.google.gson.annotations.SerializedName

data class RegisterRequest(

    @SerializedName("name")
    val name: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("ratio")
    val ratio: Double,

    @SerializedName("sensitivity")
    val sensitivity: Double,

    @SerializedName("glycemiaTarget")
    val glycemiaTarget: Int,
)