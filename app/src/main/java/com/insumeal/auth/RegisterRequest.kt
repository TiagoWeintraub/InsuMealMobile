package com.insumeal.auth
import com.google.gson.annotations.SerializedName

data class RegisterRequest(

    @SerializedName("name")
    val name: String,

    @SerializedName("lastname")
    val lastname: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("ratio")
    val ratio: String,

    @SerializedName("senitivity")
    val sensitivity: String,

    @SerializedName("glycemiaTarget")
    val glycemiaTarget: String,
)