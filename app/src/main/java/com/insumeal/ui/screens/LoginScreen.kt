package com.insumeal.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.insumeal.api.RetrofitClient
import com.insumeal.auth.LoginRequest
import com.insumeal.api.LoginService
import com.insumeal.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    context: Context,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                errorMessage = null
                isLoading = true
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val service = RetrofitClient.retrofit.create(LoginService::class.java)
                        val response = service.login(LoginRequest(email.trim(), password))
                        withContext(Dispatchers.Main) {
                            TokenManager(context).saveToken(response.accessToken)
                            isLoading = false
                            onLoginSuccess()
                        }
                    } catch (e: retrofit2.HttpException) {
                        isLoading = false
                        val errorBody = e.response()?.errorBody()?.string()
                        val statusCode = e.code()
                        Log.e("LoginScreen", "HttpException: $statusCode - $errorBody", e)
                        val userMessage = when (statusCode) {
                            400 -> "Solicitud incorrecta. Revisa los datos."
                            401 -> "Credenciales incorrectas. Verifica tu email y contraseña."
                            404 -> "Endpoint no encontrado."
                            500 -> "Error interno del servidor. Inténtalo más tarde."
                            else -> "Error ${statusCode}: ${errorBody ?: "Error al conectar con el servidor."}"
                        }
                        withContext(Dispatchers.Main) {
                            errorMessage = userMessage
                        }
                    } catch (e: java.io.IOException) {
                        isLoading = false
                        Log.e("LoginScreen", "IOException: ${e.message}", e)
                        withContext(Dispatchers.Main) {
                            errorMessage = "Error de red: No se pudo conectar al servidor. Verifica tu conexión."
                        }
                    } catch (e: Exception) {
                        isLoading = false
                        Log.e("LoginScreen", "Generic Exception: ${e.message}", e)
                        withContext(Dispatchers.Main) {
                            errorMessage = "Error inesperado: ${e.message ?: "Ocurrió un problema."}"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = { onNavigateToRegister() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}