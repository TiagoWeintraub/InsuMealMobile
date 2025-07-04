package com.insumeal.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import com.insumeal.auth.LoginResponse
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
                        Turquoise50,
                        Color.White,
                        Gray50
                    ),
                    startY = 0f,
                    endY = 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Hacer toda la pantalla scrollable para evitar que el botón se salga
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                // Logo y header moderno
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 32.dp) // Reducir padding (era 45dp)
                ) {
                    // Logo de la app - más compacto
                    Image(
                        painter = painterResource(id = R.drawable.logo_insumeal),
                        contentDescription = "Logo de InsuMeal",
                        modifier = Modifier
                            .size(120.dp) // Reducir tamaño (era 150dp)
                            .padding(bottom = 0.dp),
                        contentScale = ContentScale.Fit
                    )

                    // Título principal
                    Text(
                        text = "InsuMeal",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp // Reducir tamaño (era 42sp)
                        ),
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    // Subtítulo
                    Text(
                        text = "Gestioná tu diabetes de forma inteligente",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp // Reducir un poco (era 16sp)
                        ),
                        color = Turquoise500,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                // Tarjeta de login modernizada
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(28.dp)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp), // Reducir padding (era 40dp)
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp) // Reducir espaciado (era 20dp)
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

                        // Mensaje de error más compacto
                        if (errorMessage != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Error.copy(alpha = 0.08f)
                                ),
                                shape = RoundedCornerShape(12.dp) // Reducir radio (era 16dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp), // Reducir padding (era 16dp)
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Error,
                                        modifier = Modifier.size(16.dp) // Reducir tamaño (era 20dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp)) // Reducir espacio (era 12dp)
                                    Text(
                                        text = errorMessage!!,
                                        style = MaterialTheme.typography.bodySmall.copy( // Cambiar a bodySmall
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = Error,
                                        maxLines = 2, // Limitar líneas
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Botón de login con gradiente
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null

                                val loginRequest = LoginRequest(email, password)

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val loginResponse = RetrofitClient.retrofit.create(LoginService::class.java).login(loginRequest)

                                        val tokenManager = TokenManager(context)
                                        tokenManager.saveToken(loginResponse.accessToken)
                                        tokenManager.saveUserId(loginResponse.userId)

                                        try {
                                            val authHeader = "Bearer ${loginResponse.accessToken}"
                                            val profileResponse = RetrofitClient.retrofit.create(ProfileService::class.java).getUserProfile(authHeader, loginResponse.userId.toInt())
                                            userProfileViewModel.setUserProfile(profileResponse.toModel())
                                        } catch (e: Exception) {
                                            Log.e("LoginScreen", "Error cargando perfil: ${e.message}")
                                        }

                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            onLoginSuccess()
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            errorMessage = "Error de conexión: ${e.message}"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp), // Reducir altura (era 56dp)
                            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Turquoise500,
                                contentColor = Color.White,
                                disabledContainerColor = Turquoise600.copy(alpha = 0.6f),
                                disabledContentColor = Color.White.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp), // Reducir tamaño (era 24dp)
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Iniciar Sesión",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp // Reducir un poco (era 16sp)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp)) // Reducir espacio (era 8dp)
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp) // Reducir tamaño (era 20dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Botón de registro
                Row(
                    modifier = Modifier.padding(top = 24.dp), // Reducir padding (era 32dp)
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿No tienes cuenta? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700
                    )
                    TextButton(
                        onClick = onNavigateToRegister,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Turquoise500
                        )
                    ) {
                        Text(
                            text = "Regístrate",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
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
                tint = Turquoise500,
                modifier = Modifier.size(22.dp)
            )
        },
        shape = RoundedCornerShape(18.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Turquoise500,
            unfocusedBorderColor = Gray300,
            focusedLabelColor = Turquoise600,
            unfocusedLabelColor = Gray500,
            cursorColor = Turquoise600,
            focusedContainerColor = Color.White, // Fondo blanco
            unfocusedContainerColor = Color.White, // Fondo blanco
            focusedTextColor = Color.Black, // Texto negro
            unfocusedTextColor = Color.Black, // Texto negro
            focusedPlaceholderColor = Gray400,
            unfocusedPlaceholderColor = Gray400
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
                tint = Turquoise500,
                modifier = Modifier.size(22.dp)
            )
        },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onVisibilityToggle) {
                Icon(
                    imageVector = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (isVisible) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = Turquoise500,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        shape = RoundedCornerShape(18.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Turquoise500,
            unfocusedBorderColor = Gray300,
            focusedLabelColor = Turquoise600,
            unfocusedLabelColor = Gray500,
            cursorColor = Turquoise600,
            focusedContainerColor = Color.White, // Fondo blanco
            unfocusedContainerColor = Color.White, // Fondo blanco
            focusedTextColor = Color.Black, // Texto negro
            unfocusedTextColor = Color.Black, // Texto negro
            focusedPlaceholderColor = Gray400,
            unfocusedPlaceholderColor = Gray400
        )
    )
}
