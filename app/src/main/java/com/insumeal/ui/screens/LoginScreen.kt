package com.insumeal.ui.screens

import android.content.Context
import android.util.Log // Importa Log para debugging
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
import kotlinx.coroutines.withContext // Importa withContext

@Composable
fun LoginScreen(context: Context, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) } // Para mostrar un indicador de carga

    Column(
        modifier = Modifier
            .fillMaxSize() // Ocupa toda la pantalla
            .padding(16.dp),
        verticalArrangement = Arrangement.Center, // Centra el contenido verticalmente
        horizontalAlignment = Alignment.CenterHorizontally // Centra el contenido horizontalmente
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField( // Usamos OutlinedTextField para un look más moderno
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
            visualTransformation = PasswordVisualTransformation(), // Oculta la contraseña
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Muestra el mensaje de error si existe
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                // Limpiar mensaje de error previo
                errorMessage = null
                isLoading = true // Inicia la carga
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val service = RetrofitClient.retrofit.create(LoginService::class.java)
                        val response = service.login(LoginRequest(email.trim(), password)) // .trim() para el email

                        // Actualizar la UI y navegar DEBE hacerse en el Main thread
                        withContext(Dispatchers.Main) {
                            TokenManager(context).saveToken(response.accessToken)
                            isLoading = false // Termina la carga
                            onLoginSuccess()
                        }

                    } catch (e: retrofit2.HttpException) {
                        isLoading = false // Termina la carga en caso de error
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
                        isLoading = false // Termina la carga en caso de error
                        Log.e("LoginScreen", "IOException: ${e.message}", e)
                        withContext(Dispatchers.Main) {
                            errorMessage = "Error de red: No se pudo conectar al servidor. Verifica tu conexión."
                        }
                    } catch (e: Exception) {
                        isLoading = false // Termina la carga en caso de error
                        Log.e("LoginScreen", "Generic Exception: ${e.message}", e)
                        withContext(Dispatchers.Main) {
                            errorMessage = "Error inesperado: ${e.message ?: "Ocurrió un problema."}"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading // Deshabilita el botón mientras carga
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
    }
}