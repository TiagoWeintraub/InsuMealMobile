package com.insumeal.api

import com.google.gson.annotations.SerializedName

data class UpdateGramsRequest(
    @SerializedName("grams")
    val grams: Double
)
