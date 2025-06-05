package com.insumeal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insumeal.api.ClinicalDataService
import com.insumeal.models.ClinicalData
import com.insumeal.schemas.ClinicalDataSchema
import com.insumeal.schemas.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ClinicalDataViewModel : ViewModel() {
    private val _clinicalData = MutableStateFlow<ClinicalData?>(null)
    val clinicalData: StateFlow<ClinicalData?> = _clinicalData

    private val clinicalDataService: ClinicalDataService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.0.170:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClinicalDataService::class.java)
    }

    fun loadClinicalData(authHeader: String, userId: String) {
        viewModelScope.launch {
            try {
                val response = clinicalDataService.getClinicalData(authHeader, userId.toInt())
                _clinicalData.value = response.toModel()
            } catch (e: Exception) {
                _clinicalData.value = null
            }
        }
    }

    fun clearClinicalData() {
        _clinicalData.value = null
    }

    fun setClinicalData(data: ClinicalData) {
        _clinicalData.value = data
    }
}
