package com.insumeal.api

import com.insumeal.schemas.ClinicalDataSchema
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ClinicalDataService {
    @GET("/clinical_data/{user_id}")
    suspend fun getClinicalData(
        @Header("Authorization") authHeader: String,
        @Path("user_id") userId: Int
    ): ClinicalDataSchema
}
