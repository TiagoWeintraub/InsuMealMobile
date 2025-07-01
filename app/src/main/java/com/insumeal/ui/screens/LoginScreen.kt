package com.insumeal.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.insumeal.api.RetrofitClient
import com.insumeal.auth.LoginRequest
import com.insumeal.api.LoginService
import com.insumeal.utils.TokenManager
import com.insumeal.ui.viewmodel.UserProfileViewModel
import com.insumeal.api.ProfileService
import com.insumeal.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.insumeal.schemas.toModel
import com.insumeal.R

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
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Orange50,
                        Color.White,
                        Gray50
                    ),
                    startY = 0f,
                    endY = 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo y header moderno
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 45.dp)
            ) {
                // Logo de la app - más grande y sin card
                Image(
                    painter = painterResource(id = R.drawable.logo_insumeal),
                    contentDescription = "Logo de InsuMeal",
                    modifier = Modifier
                        .size(150.dp) // Tamaño mucho más grande
                        .padding(bottom = 0.dp), // Padding solo en la parte inferior
                    contentScale = ContentScale.Fit // Mantener proporciones
                )

                // Título principal
                Text(
                    text = "InsuMeal",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 42.sp
                    ),
                    color = Orange600,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Bienvenido de vuelta",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp
                    ),
                    color = Gray700,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Gestiona tu diabetes de forma inteligente",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Formulario de login moderno
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = Orange500.copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Reducido de 24.dp a 16.dp
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Gray800,
                        textAlign = TextAlign.Center
                    )

                    // Campo de email moderno
                    ModernLoginTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo electrónico",
                        leadingIcon = Icons.Default.Email,
                        placeholder = "ejemplo@correo.com"
                    )

                    // Campo de contraseña moderno
                    ModernLoginPasswordField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        isVisible = passwordVisible,
                        onVisibilityToggle = { passwordVisible = !passwordVisible },
                        placeholder = "Ingresa tu contraseña"
                    )

                    // Mensaje de error moderno
                    if (errorMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Error.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp)) // Reducido de 8.dp a 4.dp

                    // Botón de iniciar sesión moderno
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
                                    }

                                    // Obtener perfil usando el token
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
                            .height(64.dp),
                        shape = RoundedCornerShape(18.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Orange600,
                            disabledContainerColor = Orange600.copy(alpha = 0.6f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(26.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Login,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Iniciar Sesión",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sección de registro moderna
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Gray50.copy(alpha = 0.7f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿No tienes cuenta?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray600,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Crear cuenta nueva",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Orange600
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Orange600,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Componentes modernos para el login
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernLoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        },
        placeholder = {
            Text(
                placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray400
            )
        },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = Orange500,
                modifier = Modifier.size(22.dp)
            )
        },
        shape = RoundedCornerShape(18.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Orange500,
            unfocusedBorderColor = Gray300,
            focusedLabelColor = Orange600,
            unfocusedLabelColor = Gray500,
            cursorColor = Orange600,
            focusedContainerColor = Orange50.copy(alpha = 0.3f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernLoginPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        },
        placeholder = {
            Text(
                placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray400
            )
        },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Orange500,
                modifier = Modifier.size(22.dp)
            )
        },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onVisibilityToggle) {
                Icon(
                    imageVector = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (isVisible) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = Orange500,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        shape = RoundedCornerShape(18.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Orange500,
            unfocusedBorderColor = Gray300,
            focusedLabelColor = Orange600,
            unfocusedLabelColor = Gray500,
            cursorColor = Orange600,
            focusedContainerColor = Orange50.copy(alpha = 0.3f)
        )
    )
}
