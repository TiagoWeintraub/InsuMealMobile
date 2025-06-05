package com.insumeal.schemas

import com.google.gson.annotations.SerializedName

// Este schema representa la estructura de la respuesta del endpoint /users/get_by_id/{user_id}
data class UserProfileSchema(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String
)

fun UserProfileSchema.toModel(): com.insumeal.models.UserProfile {
    return com.insumeal.models.UserProfile(
        id = this.id,
        username = this.name,
        lastName = this.lastName,
        email = this.email
    )
}
