package com.insumeal.api

import android.content.Context
import android.util.Log
import com.insumeal.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object RetrofitClient {
    const val DEFAULT_BASE_URL = "https://insumeal-backend-ingress.my.kube.um.edu.ar/"
    private var tokenManager: TokenManager? = null
    @Volatile
    private var currentBaseUrl: String = DEFAULT_BASE_URL

    // Función para obtener la URL completa de imágenes de meal plates
    fun getMealPlateImageUrl(mealPlateId: Int): String {
        return "${currentBaseUrl}meal_plate/image/$mealPlateId"
    }

    fun getCurrentBaseUrl(): String = currentBaseUrl

    @Synchronized
    fun updateBaseUrlFromHostPort(hostPort: String): Boolean {
        val trimmed = hostPort.trim()
        if (trimmed.isBlank()) {
            return false
        }

        return try {
            val normalized = normalizeBaseUrl(trimmed)
            currentBaseUrl = normalized
            retrofitInstance = buildRetrofit(normalized)
            Log.d("RetrofitClient", "Backend actualizado en runtime: $normalized")
            true
        } catch (e: Exception) {
            Log.e("RetrofitClient", "URL de backend invalida: $hostPort", e)
            false
        }
    }

    @Synchronized
    fun resetBaseUrl() {
        currentBaseUrl = DEFAULT_BASE_URL
        retrofitInstance = buildRetrofit(currentBaseUrl)
        Log.d("RetrofitClient", "Backend reseteado al ingress por defecto")
    }

    private fun normalizeBaseUrl(raw: String): String {
        val withScheme = if (raw.startsWith("http://") || raw.startsWith("https://")) {
            raw
        } else {
            "http://$raw"
        }
        return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
    }

    // Inicializa el TokenManager con el contexto
    fun initialize(context: Context) {
        tokenManager = TokenManager(context)
    }

    // Interceptor personalizado para añadir el token JWT
    private class AuthInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val token = tokenManager?.getToken()

            // Si hay un token, lo añadimos como header de autorización
            return if (token != null) {
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                Log.d("RetrofitClient", "Añadiendo token de autorización: Bearer ${token.take(10)}...")
                chain.proceed(newRequest)
            } else {
                Log.e("RetrofitClient", "No se encontró token de autenticación")
                // Si no hay token, usamos la petición original
                chain.proceed(originalRequest)
            }
        }
    }    // Cliente HTTP con el interceptor de autenticación
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS) // 2 minutos para la respuesta
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 30 segundos para la conexión
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 30 segundos para la escritura
            .build()
    }

    private fun buildRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Volatile
    private var retrofitInstance: Retrofit = buildRetrofit(currentBaseUrl)

    // Cliente Retrofit actual, reconstruible cuando cambia la base URL
    val retrofit: Retrofit
        get() = retrofitInstance
}