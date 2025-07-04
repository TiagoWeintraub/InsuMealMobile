package com.insumeal.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insumeal.api.MealPlateApiClient
import com.insumeal.models.MealPlateHistory
import com.insumeal.services.TranslationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MealPlateHistoryViewModel : ViewModel() {
    
    private val _historyList = MutableStateFlow<List<MealPlateHistory>>(emptyList())
    val historyList: StateFlow<List<MealPlateHistory>> = _historyList
    
    private val _isLoading = MutableStateFlow(false) // Cambiar a false inicialmente
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    private val apiClient = MealPlateApiClient()
    private val translationService = TranslationService.getInstance()
    
    @SuppressLint("SuspiciousIndentation")
    fun loadHistory(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true // Activar loading al inicio de la función
            _errorMessage.value = null
            _historyList.value = emptyList() // Limpiar lista anterior

            try {
                val result = apiClient.getMealPlateHistory(context)
                result.onSuccess { history ->
                    // Traducir el historial automáticamente a español
                    viewModelScope.launch {
                        try {
                            val translatedHistory = translationService.translateMealPlateHistoryListToSpanish(history)
                            _historyList.value = translatedHistory
                        } catch (e: Exception) {
                            android.util.Log.e("MealPlateHistoryViewModel", "Error en traducción: ${e.message}", e)
                            // Si falla la traducción, usar el historial original
                            _historyList.value = history
                        } finally {
                            _isLoading.value = false // Desactivar loading después de la traducción
                        }
                    }
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
                    _isLoading.value = false // Desactivar loading en caso de error
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false // Desactivar loading en caso de excepción
            }
        }
    }      fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Inicializa el servicio de traducción
     */
    fun initializeTranslationService() {
        viewModelScope.launch {
            try {
                translationService.initialize()
                android.util.Log.d("MealPlateHistoryViewModel", "Servicio de traducción inicializado")
            } catch (e: Exception) {
                android.util.Log.e("MealPlateHistoryViewModel", "Error inicializando servicio de traducción: ${e.message}", e)
            }
        }
    }
    
    fun deleteMealPlate(context: Context, mealPlateId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = apiClient.deleteMealPlate(context, mealPlateId)
                
                result.onSuccess {
                    // Remover el elemento de la lista local
                    _historyList.value = _historyList.value.filter { it.id != mealPlateId }
                    onSuccess()
                }.onFailure { error ->
                    val errorMsg = when {
                        error.message?.contains("401") == true -> 
                            "Error de autenticación. Tu sesión ha expirado."
                        error.message?.contains("403") == true -> 
                            "No tienes permiso para eliminar este elemento."
                        error.message?.contains("404") == true -> 
                            "El elemento ya no existe."
                        error.message?.contains("timeout") == true -> 
                            "El servidor está tardando demasiado en responder."
                        error.message?.contains("host") == true -> 
                            "No se puede conectar al servidor. Verifica tu conexión a internet."
                        else -> "Error al eliminar: ${error.message}"
                    }
                    onError(errorMsg)
                }            } catch (e: Exception) {
                onError("Error inesperado: ${e.message}")
            }
        }
    }
    
    fun deleteAllMealPlates(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = apiClient.deleteAllMealPlates(context)
                
                result.onSuccess {
                    // Limpiar la lista local
                    _historyList.value = emptyList()
                    onSuccess()
                }.onFailure { error ->
                    val errorMsg = when {
                        error.message?.contains("401") == true -> 
                            "Error de autenticación. Tu sesión ha expirado."
                        error.message?.contains("403") == true -> 
                            "No tienes permiso para eliminar el historial."
                        error.message?.contains("404") == true -> 
                            "No hay historial para eliminar."
                        error.message?.contains("timeout") == true -> 
                            "El servidor está tardando demasiado en responder."
                        error.message?.contains("host") == true -> 
                            "No se puede conectar al servidor. Verifica tu conexión a internet."
                        else -> "Error al eliminar historial: ${error.message}"
                    }
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                onError("Error inesperado: ${e.message}")
            }
        }
    }
}
