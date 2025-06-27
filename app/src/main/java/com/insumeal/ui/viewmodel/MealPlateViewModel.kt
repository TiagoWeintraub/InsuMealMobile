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
import com.insumeal.models.DosisCalculation
import com.insumeal.services.TranslationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MealPlateViewModel : ViewModel() {
    // Para la imagen
    var imageUri by mutableStateOf<Uri?>(null)
    var bitmap by mutableStateOf<Bitmap?>(null)
    
    // API Client para las operaciones de API
    private val apiClient = MealPlateApiClient()
    
    // Servicio de traducción
    private val translationService = TranslationService.getInstance()
    
    // Para el estado de carga usando StateFlow
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    // Para los datos del plato usando StateFlow
    private val _mealPlate = MutableStateFlow<MealPlate?>(null)
    val mealPlate: StateFlow<MealPlate?> = _mealPlate      // Estado para saber si hemos intentado cargar datos usando StateFlow
    private val _hasAttemptedLoad = MutableStateFlow(false)
    val hasAttemptedLoad: StateFlow<Boolean> = _hasAttemptedLoad
    
    // Para los datos del cálculo de dosis usando StateFlow
    private val _dosisCalculation = MutableStateFlow<DosisCalculation?>(null)
    val dosisCalculation: StateFlow<DosisCalculation?> = _dosisCalculation
    
    // Para preservar la glucemia ingresada por el usuario
    private val _lastGlycemia = MutableStateFlow<String>("")
    val lastGlycemia: StateFlow<String> = _lastGlycemia
    
    fun setImage(uri: Uri?, bmp: Bitmap?) {
        imageUri = uri
        bitmap = bmp
    }fun clearData() {
        _mealPlate.value = null
        _errorMessage.value = null
        _hasAttemptedLoad.value = false
        _isLoading.value = false
        imageUri = null
        bitmap = null
    }    
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Función para guardar la glucemia ingresada
     */
    fun saveGlycemia(glycemia: String) {
        _lastGlycemia.value = glycemia
    }
    
    /**
     * Función para limpiar la glucemia guardada
     */
    fun clearGlycemia() {
        _lastGlycemia.value = ""
    }
    
    /**
     * Inicializa el servicio de traducción
     */
    fun initializeTranslationService() {
        viewModelScope.launch {
            try {
                translationService.initialize()
                android.util.Log.d("MealPlateViewModel", "Servicio de traducción inicializado")
            } catch (e: Exception) {
                android.util.Log.e("MealPlateViewModel", "Error inicializando servicio de traducción: ${e.message}", e)
            }
        }
    }
    
    fun analyzeImage(context: Context, onSuccess: () -> Unit, onNoFoodDetected: () -> Unit = {}) {
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
                    android.util.Log.d("MealPlateViewModel", "Recibido plato con éxito: \${plate.name}, ingredientes: \${plate.ingredients.size}, id: ${plate.id}")

                    // Verificar si el backend indica que no se detectaron alimentos (ID = -1)
                    if (plate.id == -1) {
                        android.util.Log.d("MealPlateViewModel", "No se detectaron alimentos en la imagen (ID = -1)")
                        onNoFoodDetected()
                        return@onSuccess
                    }

                    // Traducir el plato automáticamente a español
                    viewModelScope.launch {
                        try {
                            val translatedPlate = translationService.translateMealPlateToSpanish(plate)
                            _mealPlate.value = translatedPlate
                            android.util.Log.d("MealPlateViewModel", "Plato traducido: ${translatedPlate.name}")
                            onSuccess()
                        } catch (e: Exception) {
                            android.util.Log.e("MealPlateViewModel", "Error en traducción: ${e.message}", e)
                            // Si falla la traducción, usar el plato original
                            _mealPlate.value = plate
                            onSuccess()
                        }
                    }
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
    }    fun updateIngredientGrams(
        context: Context,
        mealPlateId: Int,
        ingredientId: Int,
        newGrams: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("MealPlateViewModel", "Actualizando ingrediente: mealPlateId=$mealPlateId, ingredientId=$ingredientId, newGrams=$newGrams")
                
                // Obtener el MealPlate actual
                val currentMealPlate = _mealPlate.value
                if (currentMealPlate == null) {
                    onError("No se encontró información del plato")
                    return@launch
                }
                
                val result = apiClient.updateIngredientGrams(context, currentMealPlate, ingredientId, newGrams)
                
                result.onSuccess { updatedMealPlate ->
                    android.util.Log.d("MealPlateViewModel", "Ingrediente actualizado con éxito")
                    _mealPlate.value = updatedMealPlate
                    onSuccess()
                }.onFailure { error ->
                    android.util.Log.e("MealPlateViewModel", "Error al actualizar ingrediente: ${error.message}", error)
                    val errorMsg = when {
                        error.message?.contains("401") == true -> 
                            "Error de autenticación. Tu sesión ha expirado."
                        error.message?.contains("403") == true -> 
                            "No tienes permiso para realizar esta acción."
                        error.message?.contains("404") == true -> 
                            "No se encontró el ingrediente o plato especificado."
                        error.message?.contains("timeout") == true -> 
                            "El servidor está tardando demasiado en responder."
                        error.message?.contains("host") == true -> 
                            "No se puede conectar al servidor. Verifica tu conexión."
                        else -> "Error al actualizar: ${error.message}"
                    }
                    onError(errorMsg)
                }
            } catch (e: Exception) {            android.util.Log.e("MealPlateViewModel", "Error inesperado al actualizar ingrediente: ${e.message}", e)
                onError("Error inesperado: ${e.message}")
            }
        }
    }    fun calculateDosis(
        context: Context,
        mealPlateId: Int,
        glycemia: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("MealPlateViewModel", "Calculando dosis para mealPlateId=$mealPlateId con glycemia=$glycemia")
                _isLoading.value = true
                  // Guardar la glucemia ingresada antes de hacer el cálculo (como entero)
                _lastGlycemia.value = glycemia.toInt().toString()
                
                val result = apiClient.calculateDosis(context, mealPlateId, glycemia)
                
                result.onSuccess { dosisCalculation ->
                    android.util.Log.d("MealPlateViewModel", "Cálculo de dosis exitoso")
                    _dosisCalculation.value = dosisCalculation
                    onSuccess()
                }.onFailure { error ->
                    android.util.Log.e("MealPlateViewModel", "Error al calcular dosis: ${error.message}", error)
                    val errorMsg = when {
                        error.message?.contains("401") == true -> 
                            "Error de autenticación. Tu sesión ha expirado."
                        error.message?.contains("403") == true -> 
                            "No tienes permiso para realizar esta acción."
                        error.message?.contains("404") == true -> 
                            "No se encontró el plato especificado."
                        error.message?.contains("timeout") == true -> 
                            "El servidor está tardando demasiado en responder."
                        error.message?.contains("host") == true -> 
                            "No se puede conectar al servidor. Verifica tu conexión a internet."
                        else -> "Error al calcular la dosis: ${error.message}"
                    }
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                android.util.Log.e("MealPlateViewModel", "Error inesperado al calcular dosis: ${e.message}", e)
                onError("Error inesperado: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteMealPlate(
        context: Context,
        mealPlateId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("MealPlateViewModel", "Eliminando meal plate con ID: $mealPlateId")
                _isLoading.value = true
                
                val result = apiClient.deleteMealPlate(context, mealPlateId)
                  result.onSuccess {
                    android.util.Log.d("MealPlateViewModel", "Meal plate eliminado exitosamente")
                    // Ejecutar onSuccess primero para navegar, luego limpiar datos
                    onSuccess()
                    // Limpiar el estado del ViewModel después de navegar
                    clearData()
                }.onFailure { error ->
                    android.util.Log.e("MealPlateViewModel", "Error al eliminar meal plate: ${error.message}", error)
                    val errorMsg = when {
                        error.message?.contains("401") == true -> 
                            "Error de autenticación. Tu sesión ha expirado."
                        error.message?.contains("403") == true -> 
                            "No tienes permiso para eliminar este plato."
                        error.message?.contains("404") == true -> 
                            "El plato ya no existe o no se pudo encontrar."
                        error.message?.contains("timeout") == true -> 
                            "El servidor está tardando demasiado en responder."
                        error.message?.contains("host") == true -> 
                            "No se puede conectar al servidor. Verifica tu conexión a internet."
                        else -> "Error al eliminar el plato: ${error.message}"
                    }
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                android.util.Log.e("MealPlateViewModel", "Error inesperado al eliminar meal plate: ${e.message}", e)
                onError("Error inesperado: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }    fun deleteIngredientFromMealPlate(
        context: Context,
        mealPlateId: Int,
        ingredientId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("MealPlateViewModel", "Eliminando ingrediente: mealPlateId=$mealPlateId, ingredientId=$ingredientId")
                
                // Obtener el MealPlate actual
                val currentMealPlate = _mealPlate.value
                if (currentMealPlate == null) {
                    onError("No se encontró información del plato")
                    return@launch
                }
                
                android.util.Log.d("MealPlateViewModel", "Ingredientes antes de eliminar: ${currentMealPlate.ingredients.size}")
                currentMealPlate.ingredients.forEach { ingredient ->
                    android.util.Log.d("MealPlateViewModel", "Ingrediente: ${ingredient.name} (ID: ${ingredient.id})")
                }
                
                val result = apiClient.deleteIngredientFromMealPlate(context, currentMealPlate, ingredientId)
                
                result.onSuccess { updatedMealPlate ->
                    android.util.Log.d("MealPlateViewModel", "Ingrediente eliminado con éxito")
                    android.util.Log.d("MealPlateViewModel", "Ingredientes después de eliminar: ${updatedMealPlate.ingredients.size}")
                    updatedMealPlate.ingredients.forEach { ingredient ->
                        android.util.Log.d("MealPlateViewModel", "Ingrediente restante: ${ingredient.name} (ID: ${ingredient.id})")
                    }
                    
                    _mealPlate.value = updatedMealPlate
                    android.util.Log.d("MealPlateViewModel", "Estado del MealPlate actualizado en el ViewModel")
                    onSuccess()
                }.onFailure { error ->
                    android.util.Log.e("MealPlateViewModel", "Error al eliminar ingrediente: ${error.message}", error)
                    val errorMsg = when {
                        error.message?.contains("401") == true -> 
                            "Error de autenticación. Tu sesión ha expirado."
                        error.message?.contains("403") == true -> 
                            "No tienes permiso para realizar esta acción."
                        error.message?.contains("404") == true -> 
                            "No se encontró el ingrediente o plato especificado."
                        error.message?.contains("timeout") == true -> 
                            "El servidor está tardando demasiado en responder."
                        error.message?.contains("host") == true -> 
                            "No se puede conectar al servidor. Verifica tu conexión."
                        else -> "Error al eliminar ingrediente: ${error.message}"
                    }
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                android.util.Log.e("MealPlateViewModel", "Error inesperado al eliminar ingrediente: ${e.message}", e)
                onError("Error inesperado: ${e.message}")
            }
        }
    }    fun addFoodToMealPlate(
        context: Context,
        mealPlateId: Int,
        foodName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("MealPlateViewModel", "Agregando alimento '$foodName' al meal plate ID: $mealPlateId")
                
                val result = apiClient.addFoodToMealPlate(context, mealPlateId, foodName)
                
                result.onSuccess { updatedMealPlate ->
                    android.util.Log.d("MealPlateViewModel", "Alimento agregado exitosamente")

                    // Traducir el meal plate actualizado antes de actualizar el estado
                    viewModelScope.launch {
                        try {
                            val translatedMealPlate = translationService.translateMealPlateToSpanish(updatedMealPlate)
                            android.util.Log.d("MealPlateViewModel", "Meal plate traducido después de agregar alimento")

                            // Actualizar el estado del meal plate con los nuevos datos traducidos
                            _mealPlate.value = translatedMealPlate
                            onSuccess()
                        } catch (e: Exception) {
                            android.util.Log.e("MealPlateViewModel", "Error en traducción después de agregar alimento: ${e.message}", e)
                            // Si falla la traducción, usar el meal plate sin traducir
                            _mealPlate.value = updatedMealPlate
                            onSuccess()
                        }
                    }
                }.onFailure { error ->
                    android.util.Log.e("MealPlateViewModel", "Error al agregar alimento: ${error.message}", error)
                    val errorMsg = when {
                        error.message?.contains("FOOD_ALREADY_EXISTS") == true -> 
                            "Este alimento ya está en el plato"
                        error.message?.contains("MealPlate no encontrado") == true -> 
                            "El plato no existe o no se pudo encontrar"
                        error.message?.contains("401") == true -> 
                            "Error de autenticación. Tu sesión ha expirado."
                        error.message?.contains("403") == true -> 
                            "No tienes permiso para agregar alimentos a este plato."
                        error.message?.contains("timeout") == true -> 
                            "El servidor está tardando demasiado en responder."
                        error.message?.contains("host") == true -> 
                            "No se puede conectar al servidor. Verifica tu conexión a internet."
                        else -> "Error al agregar el alimento: ${error.message}"
                    }
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                android.util.Log.e("MealPlateViewModel", "Error inesperado al agregar alimento: ${e.message}", e)
                onError("Error inesperado: ${e.message}")
            }
        }
    }
}
