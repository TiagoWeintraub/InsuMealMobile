package com.insumeal.models

import com.google.gson.annotations.SerializedName

data class ClinicalData(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("ratio")
    val ratio: Double,
    
    @SerializedName("sensitivity")
    val sensitivity: Double,
      @SerializedName("glycemiaTarget")
    val glycemiaTarget: Double,
    
    @SerializedName("user_id")
    val userId: Int
)