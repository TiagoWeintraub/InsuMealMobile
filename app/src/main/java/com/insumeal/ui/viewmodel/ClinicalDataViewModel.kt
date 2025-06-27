package com.insumeal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insumeal.api.ClinicalDataService
import com.insumeal.api.RetrofitClient
import com.insumeal.api.UpdateClinicalDataRequest
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    private val clinicalDataService: ClinicalDataService by lazy {
        RetrofitClient.retrofit.create(ClinicalDataService::class.java)
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
    }    fun updateClinicalData(
        authHeader: String,
        userId: String,
        ratio: Double,
        sensitivity: Double,
        glycemiaTarget: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val updateRequest = UpdateClinicalDataRequest(
                    ratio = ratio,
                    sensitivity = sensitivity,
                    glycemiaTarget = glycemiaTarget
                )
                
                val response = clinicalDataService.updateClinicalData(authHeader, userId.toInt(), updateRequest)
                
                if (response.isSuccessful && response.body() != null) {
                    _clinicalData.value = response.body()!!.toModel()
                    onSuccess()
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Error de autenticación"
                        403 -> "No autorizado"
                        404 -> "Usuario no encontrado"
                        500 -> "Error interno del servidor"
                        else -> "Error al actualizar: ${response.message()}"
                    }
                    onError(errorMessage)
                }
            } catch (e: Exception) {
                onError("Error de conexión: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearClinicalData() {
        _clinicalData.value = null
    }

    fun setClinicalData(data: ClinicalData) {
        _clinicalData.value = data
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
