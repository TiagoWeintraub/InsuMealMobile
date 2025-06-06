package com.insumeal.api

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.insumeal.models.MealPlate
import com.insumeal.schemas.toModel
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
                        Log.d("MealPlateApiClient", "Schema recibido: ${mealPlateSchema.name}, ${mealPlateSchema.ingredients.size} ingredientes")
                        
                        // Mostrar detalles para debug
                        Log.d("MealPlateApiClient", "Schema detallado: id=${mealPlateSchema.id}, carbs=${mealPlateSchema.totalCarbs}, dosis=${mealPlateSchema.dosis}")
                        mealPlateSchema.ingredients.forEachIndexed { index, ingredient ->
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
    }
}
