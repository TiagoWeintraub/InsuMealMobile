package com.insumeal.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insumeal.api.MealPlateApiClient
import com.insumeal.models.MealPlate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FoodHistoryMealPlateViewModel : ViewModel() {
    
    private val _mealPlate = MutableStateFlow<MealPlate?>(null)
    val mealPlate: StateFlow<MealPlate?> = _mealPlate
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    private val apiClient = MealPlateApiClient()
    
    fun loadMealPlateDetails(context: Context, mealPlateId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = apiClient.getMealPlateById(context, mealPlateId)
                
                result.onSuccess { mealPlate ->
                    _mealPlate.value = mealPlate
                }.onFailure { error ->
                    _errorMessage.value = when {
                        error.message?.contains("401") == true -> 
                            "Error de autenticación. Tu sesión ha expirado."
                        error.message?.contains("403") == true -> 
                            "No tienes permiso para acceder a esta información."
                        error.message?.contains("404") == true -> 
                            "No se encontraron los detalles del plato."
                        error.message?.contains("timeout") == true -> 
                            "El servidor está tardando demasiado en responder."
                        error.message?.contains("host") == true -> 
                            "No se puede conectar al servidor. Verifica tu conexión a internet."
                        else -> "Error al cargar los detalles: ${error.message}"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun clearData() {
        _mealPlate.value = null
        _errorMessage.value = null
        _isLoading.value = false
    }
}
