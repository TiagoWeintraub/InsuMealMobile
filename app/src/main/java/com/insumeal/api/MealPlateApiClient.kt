package com.insumeal.api

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.insumeal.models.MealPlate
import com.insumeal.models.DosisCalculation
import com.insumeal.models.MealPlateHistory
import com.insumeal.schemas.toModel
import com.insumeal.schemas.DosisCalculationSchema
import com.insumeal.utils.AuthCheckUtil
import com.insumeal.utils.ImageUtils
import com.insumeal.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MealPlateApiClient {
    // Creamos un servicio lazy para asegurarnos que RetrofitClient esté inicializado con el token
    private fun getMealPlateService(context: Context): MealPlateService {
        // Inicializar RetrofitClient con el contexto para obtener el token
        RetrofitClient.initialize(context)
        return RetrofitClient.retrofit.create(MealPlateService::class.java)
    }    suspend fun analyzeImage(context: Context, imageUri: Uri?, bitmap: Bitmap?): Result<MealPlate> {
        return withContext(Dispatchers.IO) {
            try {
                // Verificar si hay un token disponible y es válido
                if (!AuthCheckUtil.checkToken(context)) {
                    Log.e("MealPlateApiClient", "No hay token de autenticación válido")
                    return@withContext Result.failure(Exception("No hay sesión activa. Por favor inicia sesión nuevamente."))
                }
                
                // También mostramos la información del usuario para depuración
                Log.d("MealPlateApiClient", "Información de usuario: ${AuthCheckUtil.getUserInfo(context)}")
                Log.d("MealPlateApiClient", "Iniciando análisis de imagen. URI: $imageUri, Bitmap: ${bitmap != null}")
                
                // Preparar la parte de la imagen
                val imagePart = prepareImagePart(context, imageUri, bitmap)
                Log.d("MealPlateApiClient", "Imagen preparada correctamente, enviando al servidor...")
                  // Llamar al servicio con el contexto para asegurar que tenga el token
                val mealPlateService = getMealPlateService(context)
                val response = mealPlateService.analyzeImage(imagePart)
                Log.d("MealPlateApiClient", "Respuesta del servidor: ${response.code()}")
                
                // Registrar más detalles de la respuesta
                Log.d("MealPlateApiClient", "Cabeceras de respuesta: ${response.headers()}")
                Log.d("MealPlateApiClient", "Mensaje de respuesta: ${response.message()}")
                
                if (response.isSuccessful && response.body() != null) {
                    try {
                        // Convertir el schema a modelo
                        val mealPlateSchema = response.body()!!
                        Log.d("MealPlateApiClient", "Schema recibido: ${mealPlateSchema.name}, ${mealPlateSchema.ingredients?.size} ingredientes")
                        
                        // Mostrar detalles para debug
                        Log.d("MealPlateApiClient", "Schema detallado: id=${mealPlateSchema.id}, carbs=${mealPlateSchema.totalCarbs}, dosis=${mealPlateSchema.dosis}")
                        mealPlateSchema.ingredients?.forEachIndexed { index, ingredient ->
                            Log.d("MealPlateApiClient", "Ingrediente $index: ${ingredient.name}, ${ingredient.grams}g, ${ingredient.carbs}g carbs")
                        }
                        
                        // Convertir a modelo y devolver éxito
                        val mealPlateModel = mealPlateSchema.toModel()
                        Log.d("MealPlateApiClient", "Modelo creado exitosamente: ${mealPlateModel.name}, id=${mealPlateModel.id}")
                        return@withContext Result.success(mealPlateModel)
                    } catch (e: Exception) {
                        Log.e("MealPlateApiClient", "Error al convertir la respuesta a modelo", e)
                        return@withContext Result.failure(Exception("Error al procesar la respuesta del servidor: ${e.message}"))
                    }
                } else {
                    // Log más detallado para el error
                    val errorBody = response.errorBody()?.string()
                    Log.e("MealPlateApiClient", "Error en la respuesta: ${response.code()} - ${response.message()}")
                    Log.e("MealPlateApiClient", "Error body: $errorBody")
                    
                    val errorMessage = when (response.code()) {
                        401 -> "Error de autenticación. Tu sesión ha expirado, por favor inicia sesión nuevamente."
                        403 -> "No tienes permiso para realizar esta acción."
                        404 -> "El recurso solicitado no existe."
                        500 -> "Error interno del servidor. Intenta más tarde."
                        else -> "Error al analizar la imagen: ${response.code()} ${response.message()}"
                    }
                    
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e("MealPlateApiClient", "Excepción al analizar imagen", e)
                Result.failure(e)
            }
        }
    }

    private fun prepareImagePart(context: Context, imageUri: Uri?, bitmap: Bitmap?): MultipartBody.Part {
        try {
            // Usar ImageUtils para convertir a archivo
            val file = when {
                imageUri != null -> {
                    Log.d("MealPlateApiClient", "Convirtiendo URI a archivo: $imageUri")
                    ImageUtils.uriToFile(context, imageUri)
                }
                bitmap != null -> {
                    Log.d("MealPlateApiClient", "Convirtiendo Bitmap a archivo")
                    ImageUtils.bitmapToFile(context, bitmap)
                }
                else -> {
                    throw IllegalStateException("Se requiere una imagen (URI o Bitmap)")
                }
            }
              Log.d("MealPlateApiClient", "Archivo creado: ${file.absolutePath}, tamaño: ${file.length()} bytes")
            
            // Crear la parte multipart
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData("file", file.name, requestBody)
        } catch (e: Exception) {
            // Registrar el error para depuración
            Log.e("MealPlateApiClient", "Error al preparar la imagen", e)
            throw e
        }
    }    suspend fun updateIngredientGrams(
        context: Context,
        currentMealPlate: MealPlate,
        ingredientId: Int,
        grams: Double
    ): Result<MealPlate> = withContext(Dispatchers.IO) {
        try {
            Log.d("MealPlateApiClient", "Actualizando ingrediente: mealPlateId=${currentMealPlate.id}, ingredientId=$ingredientId, grams=$grams")
            
            val mealPlateService = getMealPlateService(context)
            val updateGramsRequest = UpdateGramsRequest(grams)
            
            val response = mealPlateService.updateIngredientGrams(currentMealPlate.id, ingredientId, updateGramsRequest)
            Log.d("MealPlateApiClient", "Respuesta del servidor: ${response.code()}")
            
            if (response.isSuccessful) {
                // En lugar de confiar en la respuesta del backend, actualizar localmente
                Log.d("MealPlateApiClient", "Ingrediente actualizado exitosamente en el servidor")
                
                // Buscar el ingrediente y actualizarlo
                val updatedIngredients = currentMealPlate.ingredients.map { ingredient ->
                    if (ingredient.id == ingredientId) {
                        // Calcular los nuevos carbohidratos basados en los gramos actualizados
                        val newCarbs = (grams / 100.0) * ingredient.carbsPerHundredGrams
                        ingredient.copy(grams = grams, carbs = newCarbs)
                    } else {
                        ingredient
                    }
                }
                
                // Recalcular los carbohidratos totales
                val newTotalCarbs = updatedIngredients.sumOf { it.carbs }
                
                val updatedMealPlate = currentMealPlate.copy(
                    ingredients = updatedIngredients,
                    totalCarbs = newTotalCarbs
                )
                
                Log.d("MealPlateApiClient", "MealPlate actualizado localmente: totalCarbs=$newTotalCarbs")
                return@withContext Result.success(updatedMealPlate)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("MealPlateApiClient", "Error en la actualización: código=${response.code()}, mensaje=${response.message()}, cuerpo=$errorBody")
                
                val errorMessage = when (response.code()) {
                    401 -> "Error de autenticación"
                    403 -> "No autorizado"
                    404 -> "Ingrediente o plato no encontrado"
                    500 -> "Error interno del servidor"
                    else -> "Error al actualizar: ${response.message()}"
                }
                
                return@withContext Result.failure(Exception("$errorMessage (${response.code()})"))
            }        } catch (e: Exception) {
            Log.e("MealPlateApiClient", "Error inesperado al actualizar ingrediente", e)
            return@withContext Result.failure(e)
        }
    }
      suspend fun calculateDosis(
        context: Context,
        mealPlateId: Int,
        glycemia: Double
    ): Result<DosisCalculation> = withContext(Dispatchers.IO) {
        try {
            Log.d("MealPlateApiClient", "Calculando dosis para mealPlateId=$mealPlateId con glycemia=$glycemia")
            
            val mealPlateService = getMealPlateService(context)
            val calculateDosisRequest = CalculateDosisRequest(glycemia)
            val response = mealPlateService.calculateDosis(mealPlateId, calculateDosisRequest)
            
            Log.d("MealPlateApiClient", "Respuesta del servidor: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val dosisSchema = response.body()!!
                Log.d("MealPlateApiClient", "Cálculo de dosis exitoso")
                
                val dosisCalculation = dosisSchema.toModel()
                return@withContext Result.success(dosisCalculation)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("MealPlateApiClient", "Error en el cálculo: código=${response.code()}, mensaje=${response.message()}, cuerpo=$errorBody")
                
                val errorMessage = when (response.code()) {
                    401 -> "Error de autenticación"
                    403 -> "No autorizado"
                    404 -> "Plato no encontrado"
                    500 -> "Error interno del servidor"
                    else -> "Error al calcular la dosis: ${response.message()}"
                }
                
                return@withContext Result.failure(Exception("$errorMessage (${response.code()})"))
            }        } catch (e: Exception) {
            Log.e("MealPlateApiClient", "Error inesperado al calcular dosis", e)
            return@withContext Result.failure(e)
        }
    }

    suspend fun getMealPlateHistory(context: Context): Result<List<MealPlateHistory>> = withContext(Dispatchers.IO) {
        try {
            // Verificar si hay un token disponible y es válido
            if (!AuthCheckUtil.checkToken(context)) {
                Log.e("MealPlateApiClient", "No hay token de autenticación válido")
                return@withContext Result.failure(Exception("No hay sesión activa. Por favor inicia sesión nuevamente."))
            }

            Log.d("MealPlateApiClient", "Obteniendo historial de meal plates")
            
            val mealPlateService = getMealPlateService(context)
            val response = mealPlateService.getMealPlateHistory()
            
            Log.d("MealPlateApiClient", "Respuesta del servidor: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val historySchemas = response.body()!!
                Log.d("MealPlateApiClient", "Historial obtenido exitosamente: ${historySchemas.size} elementos")
                
                val historyModels = historySchemas.map { it.toModel() }
                return@withContext Result.success(historyModels)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("MealPlateApiClient", "Error al obtener historial: código=${response.code()}, mensaje=${response.message()}, cuerpo=$errorBody")
                
                val errorMessage = when (response.code()) {
                    401 -> "Error de autenticación"
                    403 -> "No autorizado"
                    404 -> "No se encontraron datos de historial"
                    500 -> "Error interno del servidor"
                    else -> "Error al obtener historial: ${response.message()}"
                }
                
                return@withContext Result.failure(Exception("$errorMessage (${response.code()})"))
            }        } catch (e: Exception) {
            Log.e("MealPlateApiClient", "Error inesperado al obtener historial", e)
            return@withContext Result.failure(e)
        }
    }
    suspend fun deleteMealPlate(context: Context, mealPlateId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Verificar si hay un token disponible y es válido
            if (!AuthCheckUtil.checkToken(context)) {
                Log.e("MealPlateApiClient", "No hay token de autenticación válido")
                return@withContext Result.failure(Exception("No hay sesión activa. Por favor inicia sesión nuevamente."))
            }

            Log.d("MealPlateApiClient", "Eliminando meal plate con ID: $mealPlateId")
            
            val mealPlateService = getMealPlateService(context)
            val response = mealPlateService.deleteMealPlate(mealPlateId)
            
            Log.d("MealPlateApiClient", "Respuesta del servidor: ${response.code()}")
            
            if (response.isSuccessful) {
                Log.d("MealPlateApiClient", "Meal plate eliminado exitosamente")
                return@withContext Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("MealPlateApiClient", "Error al eliminar meal plate: código=${response.code()}, mensaje=${response.message()}, cuerpo=$errorBody")
                
                val errorMessage = when (response.code()) {
                    401 -> "Error de autenticación"
                    403 -> "No autorizado"
                    404 -> "Meal plate no encontrado"
                    500 -> "Error interno del servidor"
                    else -> "Error al eliminar: ${response.message()}"
                }
                
                return@withContext Result.failure(Exception("$errorMessage (${response.code()})"))
            }
        } catch (e: Exception) {
            Log.e("MealPlateApiClient", "Error inesperado al eliminar meal plate", e)
            return@withContext Result.failure(e)
        }
    }
    suspend fun getMealPlateById(context: Context, mealPlateId: Int): Result<MealPlate> = withContext(Dispatchers.IO) {
        try {
            // Verificar si hay un token disponible y es válido
            if (!AuthCheckUtil.checkToken(context)) {
                Log.e("MealPlateApiClient", "No hay token de autenticación válido")
                return@withContext Result.failure(Exception("No hay sesión activa. Por favor inicia sesión nuevamente."))
            }
            Log.d("MealPlateApiClient", "Obteniendo detalles del meal plate con ID: $mealPlateId")
            
            // Usar RetrofitClient centralizado
            val historyService = getMealPlateService(context)
            val response = historyService.getMealPlateById(mealPlateId)
            
            Log.d("MealPlateApiClient", "Respuesta del servidor: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val mealPlateSchema = response.body()!!
                Log.d("MealPlateApiClient", "Detalles del meal plate obtenidos exitosamente")
                
                val mealPlateModel = mealPlateSchema.toModel()
                return@withContext Result.success(mealPlateModel)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("MealPlateApiClient", "Error al obtener detalles: código=${response.code()}, mensaje=${response.message()}, cuerpo=$errorBody")
                
                val errorMessage = when (response.code()) {
                    401 -> "Error de autenticación"
                    403 -> "No autorizado"
                    404 -> "Meal plate no encontrado"
                    500 -> "Error interno del servidor"
                    else -> "Error al obtener detalles: ${response.message()}"
                }
                
                return@withContext Result.failure(Exception("$errorMessage (${response.code()})"))
            }
        } catch (e: Exception) {            Log.e("MealPlateApiClient", "Error inesperado al obtener detalles del meal plate", e)
            return@withContext Result.failure(e)
        }
    }
    
    suspend fun deleteAllMealPlates(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Verificar si hay un token disponible y es válido
            if (!AuthCheckUtil.checkToken(context)) {
                Log.e("MealPlateApiClient", "No hay token de autenticación válido")
                return@withContext Result.failure(Exception("No hay sesión activa. Por favor inicia sesión nuevamente."))
            }

            Log.d("MealPlateApiClient", "Eliminando todo el historial de meal plates")
            
            val mealPlateService = getMealPlateService(context)
            val response = mealPlateService.deleteAllMealPlates()
            
            Log.d("MealPlateApiClient", "Respuesta del servidor: ${response.code()}")
            
            if (response.isSuccessful) {
                Log.d("MealPlateApiClient", "Todo el historial eliminado exitosamente")
                return@withContext Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("MealPlateApiClient", "Error al eliminar historial: código=${response.code()}, mensaje=${response.message()}, cuerpo=$errorBody")
                
                val errorMessage = when (response.code()) {
                    401 -> "Error de autenticación"
                    403 -> "No autorizado para eliminar el historial"
                    404 -> "No se encontraron datos para eliminar"
                    500 -> "Error interno del servidor"
                    else -> "Error al eliminar historial: ${response.message()}"
                }
                
                return@withContext Result.failure(Exception("$errorMessage (${response.code()})"))
            }
        } catch (e: Exception) {
            Log.e("MealPlateApiClient", "Error inesperado al eliminar todo el historial", e)
            return@withContext Result.failure(e)
        }
    }
      suspend fun deleteIngredientFromMealPlate(
        context: Context,
        currentMealPlate: MealPlate,
        ingredientId: Int
    ): Result<MealPlate> = withContext(Dispatchers.IO) {
        try {
            Log.d("MealPlateApiClient", "Eliminando ingrediente: mealPlateId=${currentMealPlate.id}, ingredientId=$ingredientId")
            
            val mealPlateService = getMealPlateService(context)
            val response = mealPlateService.deleteIngredientFromMealPlate(currentMealPlate.id, ingredientId)
            
            Log.d("MealPlateApiClient", "Respuesta del servidor: ${response.code()}")
            
            if (response.isSuccessful) {
                Log.d("MealPlateApiClient", "Ingrediente eliminado exitosamente del servidor")
                
                // Como el backend no retorna el MealPlate actualizado, 
                // actualizamos localmente removiendo el ingrediente
                val updatedIngredients = currentMealPlate.ingredients.filter { it.id != ingredientId }
                Log.d("MealPlateApiClient", "Ingredientes antes: ${currentMealPlate.ingredients.size}, después: ${updatedIngredients.size}")
                
                // Recalcular los carbohidratos totales
                val newTotalCarbs = updatedIngredients.sumOf { it.carbs }
                
                val updatedMealPlate = currentMealPlate.copy(
                    ingredients = updatedIngredients,
                    totalCarbs = newTotalCarbs
                )
                
                Log.d("MealPlateApiClient", "MealPlate actualizado localmente: totalCarbs=$newTotalCarbs")
                return@withContext Result.success(updatedMealPlate)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("MealPlateApiClient", "Error al eliminar ingrediente: código=${response.code()}, mensaje=${response.message()}, cuerpo=$errorBody")
                
                val errorMessage = when (response.code()) {
                    401 -> "Error de autenticación"
                    403 -> "No autorizado"
                    404 -> "Ingrediente o plato no encontrado"
                    500 -> "Error interno del servidor"
                    else -> "Error al eliminar ingrediente: ${response.message()}"
                }
                
                return@withContext Result.failure(Exception("$errorMessage (${response.code()})"))
            }
        } catch (e: Exception) {
            Log.e("MealPlateApiClient", "Error inesperado al eliminar ingrediente", e)
            return@withContext Result.failure(e)
        }
    }

    suspend fun addFoodToMealPlate(
        context: Context,
        mealPlateId: Int,
        foodName: String
    ): Result<MealPlate> = withContext(Dispatchers.IO) {
        try {
            Log.d("MealPlateApiClient", "Agregando alimento '$foodName' al meal plate ID: $mealPlateId")

            val mealPlateService = getMealPlateService(context)
            val response = mealPlateService.addFoodToMealPlate(mealPlateId, mapOf("food" to foodName))

            Log.d("MealPlateApiClient", "Respuesta del servidor: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Log.d("MealPlateApiClient", "Alimento agregado exitosamente")

                // La respuesta contiene meal_plate_details que necesitamos convertir a MealPlate
                val mealPlateDetails = responseBody.meal_plate_details
                val mealPlateModel = mealPlateDetails.toModel()

                return@withContext Result.success(mealPlateModel)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("MealPlateApiClient", "Error al agregar alimento: código=${response.code()}, mensaje=${response.message()}, cuerpo=$errorBody")

                val errorMessage = when (response.code()) {
                    400 -> {
                        // Parsear el error específico del backend
                        if (errorBody.contains("FOOD_ALREADY_EXISTS")) {
                            "FOOD_ALREADY_EXISTS"
                        } else {
                            "Solicitud inválida: $errorBody"
                        }
                    }
                    401 -> "Error de autenticación"
                    403 -> "No autorizado"
                    404 -> "MealPlate no encontrado"
                    500 -> "Error interno del servidor"
                    else -> "Error al agregar alimento: ${response.message()}"
                }

                return@withContext Result.failure(Exception("$errorMessage (${response.code()})"))
            }
        } catch (e: Exception) {
            Log.e("MealPlateApiClient", "Error inesperado al agregar alimento", e)
            return@withContext Result.failure(e)
        }
    }
}
