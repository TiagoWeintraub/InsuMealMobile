package com.insumeal.api

import com.insumeal.schemas.ClinicalDataSchema
import retrofit2.Response
import retrofit2.http.*

interface ClinicalDataService {
    @GET("/clinical_data/{user_id}")
    suspend fun getClinicalData(
        @Header("Authorization") authHeader: String,
        @Path("user_id") userId: Int
    ): ClinicalDataSchema

    @PUT("/clinical_data/{user_id}")
    suspend fun updateClinicalData(
        @Header("Authorization") authHeader: String,
        @Path("user_id") userId: Int,
        @Body updateRequest: UpdateClinicalDataRequest
    ): Response<ClinicalDataSchema>
}

data class UpdateClinicalDataRequest(
    val ratio: Double,
    val sensitivity: Double,
    val glycemiaTarget: Double
)
