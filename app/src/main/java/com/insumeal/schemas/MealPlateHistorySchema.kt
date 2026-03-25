package com.insumeal.schemas

import com.google.gson.annotations.SerializedName
import com.insumeal.models.MealPlateHistory
import com.insumeal.api.RetrofitClient

// Esquema para la respuesta del endpoint /food_history/
data class MealPlateHistorySchema(
    @SerializedName("id") val id: Int,
    @SerializedName("date") val date: String,
    @SerializedName("type") val type: String = "unknown",
    @SerializedName("totalCarbs") val totalCarbs: Double = 0.0,
    @SerializedName("glycemia") val glycemia: Double = 0.0,
    @SerializedName("dosis") val dosis: Double = 0.0,
    @SerializedName("food_history_id") val foodHistoryId: Int,
    @SerializedName("picture") val picture: String? = null,
    @SerializedName("picture_mime_type") val pictureMimeType: String? = null
)

// Función de extensión para convertir el schema a modelo
fun MealPlateHistorySchema.toModel(): MealPlateHistory {
    return MealPlateHistory(
        id = id,
        date = date,
        type = type,
        totalCarbs = totalCarbs,
        glycemia = glycemia,
        dosis = dosis,
        imageUrl = RetrofitClient.getMealPlateImageUrl(id) // Construir la URL de la imagen
    )
}
