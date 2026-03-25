package com.insumeal.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("insumeal_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).commit()
    }

    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun clearToken() {
        prefs.edit().remove("jwt_token").commit()
    }

    fun saveUserId(userId: String) {
        prefs.edit().putString("user_id", userId).commit()
    }

    fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }

    fun clearUserId() {
        prefs.edit().remove("user_id").commit()
    }

    fun saveSession(token: String, userId: String): Boolean {
        return prefs.edit()
            .putString("jwt_token", token)
            .putString("user_id", userId)
            .commit()
    }

    fun clearSession(): Boolean {
        return prefs.edit()
            .remove("jwt_token")
            .remove("user_id")
            .commit()
    }
}