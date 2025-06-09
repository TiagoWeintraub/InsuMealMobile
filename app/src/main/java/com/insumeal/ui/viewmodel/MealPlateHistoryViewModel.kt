package com.insumeal.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insumeal.api.MealPlateApiClient
import com.insumeal.models.MealPlateHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MealPlateHistoryViewModel : ViewModel() {
    
    private val _historyList = MutableStateFlow<List<MealPlateHistory>>(emptyList())
    val historyList: StateFlow<List<MealPlateHistory>> = _historyList
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    private val apiClient = MealPlateApiClient()
    
    fun loadHistory(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = apiClient.getMealPlateHistory(context)
                
                result.onSuccess { history ->
                    _historyList.value = history
                }.onFailure { error ->
                    _errorMessage.value = when {
                        error.message?.contains("401") == true -> 
                            "Error de autenticación. Tu sesión ha expirado."
                        error.message?.contains("403") == true -> 
                            "No tienes permiso para acceder al historial."
                        error.message?.contains("404") == true -> 
                            "No se encontraron datos de historial."
                        error.message?.contains("timeout") == true -> 
                            "El servidor está tardando demasiado en responder."
                        error.message?.contains("host") == true -> 
                            "No se puede conectar al servidor. Verifica tu conexión a internet."
                        else -> "Error al cargar el historial: ${error.message}"
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
}
