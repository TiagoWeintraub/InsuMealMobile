package com.insumeal.models

import com.google.gson.annotations.SerializedName

data class Ingredient (
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("carbsPerHundredGrams")
    val carbsPerHundredGrams: Double
)