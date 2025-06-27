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
    const val BASE_URL = "http://10.0.0.149:8000"
    private var tokenManager: TokenManager? = null

    // Función para obtener la URL completa de imágenes de meal plates
    fun getMealPlateImageUrl(mealPlateId: Int): String {
        return "$BASE_URL/meal_plate/image/$mealPlateId"
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

    // Cliente Retrofit con nuestro cliente HTTP personalizado
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}