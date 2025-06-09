package com.insumeal.schemas

import com.google.gson.annotations.SerializedName
import com.insumeal.models.ClinicalData

// Este schema representa la estructura de la respuesta del endpoint /clinical_data/{user_id}
data class ClinicalDataSchema(
    @SerializedName("id") val id: Int,
    @SerializedName("ratio") val ratio: Double,
    @SerializedName("sensitivity") val sensitivity: Double,
    @SerializedName("glycemiaTarget") val glycemiaTarget: Double,
    @SerializedName("user_id") val userId: Int
)

fun ClinicalDataSchema.toModel(): ClinicalData {
    return ClinicalData(
        id = this.id,
        ratio = this.ratio,
        sensitivity = this.sensitivity,
        glycemiaTarget = this.glycemiaTarget,
        userId = this.userId
    )
}
