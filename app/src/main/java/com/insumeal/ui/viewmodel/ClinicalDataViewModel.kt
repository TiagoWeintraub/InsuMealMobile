package com.insumeal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.insumeal.api.ClinicalDataService
import com.insumeal.api.RetrofitClient
import com.insumeal.api.UpdateClinicalDataRequest
import com.insumeal.models.ClinicalData
import com.insumeal.schemas.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.gson.Gson

class ClinicalDataViewModel : ViewModel() {
    private val _clinicalData = MutableStateFlow<ClinicalData?>(null)
    val clinicalData: StateFlow<ClinicalData?> = _clinicalData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    private fun getClinicalDataService(): ClinicalDataService {
        return RetrofitClient.retrofit.create(ClinicalDataService::class.java)
    }

    fun loadClinicalData(authHeader: String, userId: String) {
        viewModelScope.launch {
            try {
                val response = getClinicalDataService().getClinicalData(authHeader, userId.toInt())
                _clinicalData.value = response.toModel()
            } catch (e: Exception) {
                _clinicalData.value = null
            }
        }
    }

    fun updateClinicalData(
        authHeader: String,
        userId: String,
        fallbackResourceId: String? = null,
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
                
                Log.d(
                    "ClinicalDataViewModel",
                    "Enviando PUT clinical_data: userId=$userId, fallbackResourceId=$fallbackResourceId, " +
                    "ratio=$ratio, sensitivity=$sensitivity, glycemiaTarget=$glycemiaTarget"
                )
                
                val requestJson = Gson().toJson(updateRequest)
                Log.d("ClinicalDataViewModel", "Request body JSON: $requestJson")
                
                // CRÍTICO: El endpoint PUT clinical_data/{id} espera el ID del recurso (PK), NO el ID del usuario.
                // Priorizamos el ID del recurso cargado actualmente (_clinicalData) o el fallback.
                val resourceId = _clinicalData.value?.id 
                    ?: fallbackResourceId?.toIntOrNull() 
                    ?: userId.toInt() // Último recurso, aunque arriesgado si no coinciden.

                Log.d("ClinicalDataViewModel", "Usando ID para PUT: $resourceId (userId=$userId, loadedId=${_clinicalData.value?.id})")

                var response = getClinicalDataService().updateClinicalData(authHeader, resourceId, updateRequest)

                // Si falla con 404 y usamos un ID distinto al userId, probamos con userId por si acaso
                // el backend en otro entorno esperara userId (poco probable con REST estándar).
                if (response.code() == 404 && resourceId != userId.toInt()) {
                    Log.w(
                        "ClinicalDataViewModel",
                        "PUT clinical_data 404 con resourceId=$resourceId. Reintentando con userId=${userId.toInt()}"
                    )
                    response = getClinicalDataService().updateClinicalData(authHeader, userId.toInt(), updateRequest)
                }
                
                if (response.isSuccessful && response.body() != null) {
                    Log.d(
                        "ClinicalDataViewModel",
                        "PUT clinical_data exitoso: response=${Gson().toJson(response.body())}"
                    )
                    
                    val responseModel = response.body()!!.toModel()
                    
                    // Validar que la respuesta contiene los valores actualizados
                    if (responseModel.ratio == ratio && 
                        responseModel.sensitivity == sensitivity &&
                        responseModel.glycemiaTarget == glycemiaTarget) {
                        _clinicalData.value = responseModel
                        Log.d(
                            "ClinicalDataViewModel",
                            "Valores actualizados correctamente en el servidor"
                        )
                        onSuccess()
                    } else {
                        Log.e(
                            "ClinicalDataViewModel",
                            "PUT devolvió 200 OK pero los valores NO coinciden. " +
                            "Enviado: ratio=$ratio, sensitivity=$sensitivity, glycemiaTarget=$glycemiaTarget. " +
                            "Recibido: ratio=${responseModel.ratio}, sensitivity=${responseModel.sensitivity}, " +
                            "glycemiaTarget=${responseModel.glycemiaTarget}"
                        )
                        onError("El servidor no persistió los cambios correctamente")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(
                        "ClinicalDataViewModel",
                        "PUT clinical_data fallo: code=${response.code()}, message=${response.message()}, " +
                        "userId=$userId, fallbackId=$fallbackResourceId, body=$errorBody, " +
                        "headers=${response.headers()}"
                    )
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
                Log.e("ClinicalDataViewModel", "Excepción en updateClinicalData", e)
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
