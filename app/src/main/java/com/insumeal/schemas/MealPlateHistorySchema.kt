package com.insumeal.schemas

import com.google.gson.annotations.SerializedName
import com.insumeal.models.MealPlateHistory

// Esquema para la respuesta del endpoint /meal_plate/
data class MealPlateHistorySchema(
    @SerializedName("id") val id: Int,
    @SerializedName("date") val date: String,
    @SerializedName("type") val type: String,
    @SerializedName("totalCarbs") val totalCarbs: Double,
    @SerializedName("glycemia") val glycemia: Double,
    @SerializedName("dosis") val dosis: Double,
    @SerializedName("image_url") val imageUrl: String
)

// Función de extensión para convertir el schema a modelo
fun MealPlateHistorySchema.toModel(): MealPlateHistory {
    return MealPlateHistory(
        id = this.id,
        date = this.date,
        type = this.type,
        totalCarbs = this.totalCarbs,
        glycemia = this.glycemia,
        dosis = this.dosis,
        imageUrl = this.imageUrl
    )
}
