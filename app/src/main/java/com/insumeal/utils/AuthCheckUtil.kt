package com.insumeal.utils

import android.content.Context
import android.util.Log

/**
 * Clase de utilidad para verificar el estado de la autenticación
 */
object AuthCheckUtil {
    private const val TAG = "AuthCheckUtil"
    
    /**
     * Verifica si el token está disponible y es válido
     * @param context Contexto de la aplicación
     * @return true si el token está disponible, false en caso contrario
     */
    fun checkToken(context: Context): Boolean {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getToken()
        
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "No hay token disponible")
            return false
        }
        
        // Registrar información básica del token para depuración
        // No mostramos el token completo por seguridad
        Log.d(TAG, "Token disponible: ${token.take(10)}...")
        
        // Podríamos agregar aquí una verificación de la validez del token
        // por ejemplo, comprobando si ha expirado si usamos JWT
        
        return true
    }
    
    /**
     * Verifica la información del usuario actual
     * @param context Contexto de la aplicación
     * @return Información del usuario o mensaje de error
     */
    fun getUserInfo(context: Context): String {
        val tokenManager = TokenManager(context)
        val userId = tokenManager.getUserId()
        
        return if (userId.isNullOrEmpty()) {
            "No hay información de usuario disponible"
        } else {
            "Usuario ID: $userId"
        }
    }
}
