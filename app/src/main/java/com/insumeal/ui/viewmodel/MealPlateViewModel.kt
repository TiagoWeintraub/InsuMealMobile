package com.insumeal.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insumeal.api.MealPlateApiClient
import com.insumeal.models.MealPlate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MealPlateViewModel : ViewModel() {
    // Para la imagen
    var imageUri by mutableStateOf<Uri?>(null)
    var bitmap by mutableStateOf<Bitmap?>(null)
    
    // Para el estado de carga usando StateFlow
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    // Para los datos del plato usando StateFlow
    private val _mealPlate = MutableStateFlow<MealPlate?>(null)
    val mealPlate: StateFlow<MealPlate?> = _mealPlate
    
    // Estado para saber si hemos intentado cargar datos usando StateFlow
    private val _hasAttemptedLoad = MutableStateFlow(false)
    val hasAttemptedLoad: StateFlow<Boolean> = _hasAttemptedLoad
    
    // API Client para las operaciones de API
    private val apiClient = MealPlateApiClient()
    fun setImage(uri: Uri?, bmp: Bitmap?) {
        imageUri = uri
        bitmap = bmp
    }    fun clearData() {
        _mealPlate.value = null
        _errorMessage.value = null
        _hasAttemptedLoad.value = false
        _isLoading.value = false
        imageUri = null
        bitmap = null
    }
    
    fun clearError() {
        _errorMessage.value = null
    }fun analyzeImage(context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _hasAttemptedLoad.value = true
            android.util.Log.d("MealPlateViewModel", "Estado inicial: isLoading=${_isLoading.value}, errorMessage=${_errorMessage.value}")
            
            try {
                // Verificar si tenemos una imagen para enviar
                if (imageUri == null && bitmap == null) {
                    _errorMessage.value = "Se requiere una imagen para el análisis."
                    _isLoading.value = false
                    return@launch
                }
                
                android.util.Log.d("MealPlateViewModel", "Llamando a apiClient.analyzeImage()")
                val result = apiClient.analyzeImage(context, imageUri, bitmap)
                android.util.Log.d("MealPlateViewModel", "Resultado recibido de apiClient")
                result.onSuccess { plate ->
                    android.util.Log.d("MealPlateViewModel", "Recibido plato con éxito: ${plate.name}, ingredientes: ${plate.ingredients.size}")
                    _mealPlate.value = plate
                    android.util.Log.d("MealPlateViewModel", "Estado después de asignar: mealPlate=${_mealPlate.value?.name}")
                    android.util.Log.d("MealPlateViewModel", "Verificación inmediata: mealPlate es null=${_mealPlate.value == null}")
                    onSuccess()
                }.onFailure { error ->
                    // Log para ayudar a depurar el problema
                    android.util.Log.e("MealPlateViewModel", "Error: ${error.message}", error)
                      // Mensaje de error más amigable para el usuario
                    _errorMessage.value = when {
                        error.message?.contains("401") == true -> 
                            "Error de autenticación con el servidor. Tu sesión ha expirado, inicia sesión nuevamente."
                        error.message?.contains("403") == true -> 
                            "No tienes permiso para realizar esta acción. Verifica tu usuario."
                        error.message?.contains("timeout") == true -> 
                            "El servidor está tardando demasiado en responder. Por favor, inténtalo de nuevo más tarde."
                        error.message?.contains("host") == true -> 
                            "No se puede conectar al servidor. Verifica tu conexión a internet."
                        error is java.io.IOException -> 
                            "Error al procesar la imagen. Intenta con otra imagen."
                        else -> "Error: ${error.message}"
                    }
                }
            } catch (e: Exception) {
                // Log para ayudar a depurar el problema
                android.util.Log.e("MealPlateViewModel", "Error inesperado: ${e.message}", e)
                
                _errorMessage.value = "Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

