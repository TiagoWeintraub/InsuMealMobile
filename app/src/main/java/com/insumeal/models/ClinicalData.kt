package com.insumeal.models

import com.google.gson.annotations.SerializedName

data class ClinicalData(
    @SerializedName("glycemia")
    val glycemia: Double,

    @SerializedName("ratio")
    val ratio: Double,

    @SerializedName("sensitivity")
    val sensitivity: Double,

    @SerializedName("glycemiaTarget")
    val glycemiaTarget: Int,
)