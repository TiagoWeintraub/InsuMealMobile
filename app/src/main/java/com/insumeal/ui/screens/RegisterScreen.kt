
package com.insumeal.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.insumeal.api.RegisterService
import com.insumeal.api.RetrofitClient
import com.insumeal.auth.RegisterRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var ratio by remember { mutableStateOf(15.0) }
    var sensitivity by remember { mutableStateOf(50.0) }
    var glycemiaTarget by remember { mutableStateOf(100) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registro", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = ratio.toString(),
            onValueChange = { ratio = it.toDoubleOrNull() ?: 15.0 },
            label = { Text("Ratio (g/U)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = sensitivity.toString(),
            onValueChange = { sensitivity = it.toDoubleOrNull() ?: 50.0 },
            label = { Text("Sensibilidad (mg/dL)/U)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = glycemiaTarget.toString(),
            onValueChange = { glycemiaTarget = it.toIntOrNull() ?: 100 },
            label = { Text("Objetivo de glucosa (mg/dL)") },
            modifier = Modifier.fillMaxWidth()
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
                        val service = RetrofitClient.retrofit.create(RegisterService::class.java)
                        val request = RegisterRequest(
                            name = name.trim(),
                            lastName = lastName.trim(),
                            email = email.trim(),
                            password = password,
                            ratio = ratio,
                            sensitivity = sensitivity,
                            glycemiaTarget = glycemiaTarget
                        )
                        service.register(request)
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            onRegisterSuccess()
                        }
                    } catch (e: retrofit2.HttpException) {
                        isLoading = false
                        val errorBody = e.response()?.errorBody()?.string()
                        val statusCode = e.code()
                        Log.e("RegisterScreen", "HttpException: $statusCode - $errorBody", e)
                        val userMessage = when (statusCode) {
                            400 -> "Solicitud incorrecta. Revisa los datos."
                            409 -> "El correo ya está registrado."
                            500 -> "Error interno del servidor. Inténtalo más tarde."
                            else -> "Error ${statusCode}: ${errorBody ?: "Error al conectar con el servidor."}"
                        }
                        withContext(Dispatchers.Main) {
                            errorMessage = userMessage
                        }
                    } catch (e: java.io.IOException) {
                        isLoading = false
                        Log.e("RegisterScreen", "IOException: ${e.message}", e)
                        withContext(Dispatchers.Main) {
                            errorMessage = "Error de red: No se pudo conectar al servidor. Verifica tu conexión."
                        }
                    } catch (e: Exception) {
                        isLoading = false
                        Log.e("RegisterScreen", "Generic Exception: ${e.message}", e)
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
                Text("Registrarse")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = { onBackToLogin() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}