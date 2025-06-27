package com.insumeal.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.insumeal.api.RetrofitClient
import com.insumeal.auth.LoginRequest
import com.insumeal.api.LoginService
import com.insumeal.utils.TokenManager
import com.insumeal.ui.viewmodel.UserProfileViewModel
import com.insumeal.api.ProfileService
import androidx.compose.foundation.background
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.insumeal.schemas.toModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    context: Context,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val userProfileViewModel = remember { UserProfileViewModel() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 32.dp)
                    .clip(RoundedCornerShape(16.dp))                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,  // Celeste
                                MaterialTheme.colorScheme.secondary // Verde
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "InsuMeal",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Formulario en una tarjeta con sombra
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface  // Usar GrisClaro para el fondo
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Campo de email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Campo de contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Contraseña",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff
                            val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = image,
                                    contentDescription = description,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Mensaje de error
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Botón de iniciar sesión
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
                                        TokenManager(context).saveUserId(response.userId)
                                    }                                    // Obtener perfil usando el token
                                    val profileService = RetrofitClient.retrofit.create(ProfileService::class.java)
                                    val token = response.accessToken
                                    val userIdStr = response.userId
                                    val authHeader = "Bearer $token"
                                    val userProfile = profileService.getUserProfile(authHeader, userIdStr.toInt())
                                    withContext(Dispatchers.Main) {
                                        userProfileViewModel.setUserProfile(userProfile.toModel())
                                    }
                                    withContext(Dispatchers.Main) {
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Iniciar Sesión")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para ir a registro
            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    "¿No tienes cuenta? Regístrate",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}
