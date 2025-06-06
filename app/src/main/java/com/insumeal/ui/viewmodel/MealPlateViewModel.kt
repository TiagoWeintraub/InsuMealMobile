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
import kotlinx.coroutines.launch

class MealPlateViewModel : ViewModel() {
    // Para la imagen
    var imageUri by mutableStateOf<Uri?>(null)
    var bitmap by mutableStateOf<Bitmap?>(null)
    
    // Para el estado de carga
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    // Para los datos del plato
    var mealPlate by mutableStateOf<MealPlate?>(null)
    
    // API Client para las operaciones de API
    private val apiClient = MealPlateApiClient()

    fun setImage(uri: Uri?, bmp: Bitmap?) {
        imageUri = uri
        bitmap = bmp
    }    fun analyzeImage(context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            try {
                // Verificar si tenemos una imagen para enviar
                if (imageUri == null && bitmap == null) {
                    errorMessage = "Se requiere una imagen para el análisis."
                    isLoading = false
                    return@launch
                }
                
                val result = apiClient.analyzeImage(context, imageUri, bitmap)
                
                result.onSuccess { plate ->
                    mealPlate = plate
                    onSuccess()                }.onFailure { error ->
                    // Log para ayudar a depurar el problema
                    android.util.Log.e("MealPlateViewModel", "Error: ${error.message}", error)
                      // Mensaje de error más amigable para el usuario
                    errorMessage = when {
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
                
                errorMessage = "Error inesperado: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}

