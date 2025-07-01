package com.insumeal.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.insumeal.api.RegisterService
import com.insumeal.api.RetrofitClient
import com.insumeal.auth.RegisterRequest
import com.insumeal.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.insumeal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var ratio by remember { mutableStateOf(15) }
    var sensitivity by remember { mutableStateOf(30) }
    var glycemiaTarget by remember { mutableStateOf(100) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Gray50,
                        Color.White
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Header moderno y elegante
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                // Logo de la app - más grande y sin card
                Image(
                    painter = painterResource(id = R.drawable.logo_insumeal),
                    contentDescription = "Logo de InsuMeal",
                    modifier = Modifier
                        .size(120.dp) // Tamaño mucho más grande
                        .padding(bottom = 0.dp), // Padding solo en la parte inferior
                    contentScale = ContentScale.Fit // Mantener proporciones
                )

                // Logo/Título principal
                Text(
                    text = "InsuMeal",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    ),
                    color = Orange600,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Crea tu cuenta",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    ),
                    color = Gray700,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Gestiona tu diabetes de forma inteligente",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray600,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Formulario moderno en tarjeta elegante
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Orange500.copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Sección Información Personal
                    SectionHeader(
                        title = "Información Personal",
                        icon = Icons.Default.Person
                    )

                    // Campos de información personal en fila
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ModernTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Nombre",
                            leadingIcon = Icons.Default.Person,
                            modifier = Modifier.weight(1f)
                        )

                        ModernTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = "Apellido",
                            leadingIcon = Icons.Default.Person,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    ModernTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo electrónico",
                        leadingIcon = Icons.Default.Email
                    )

                    // Sección Seguridad
                    SectionHeader(
                        title = "Seguridad",
                        icon = Icons.Default.Lock
                    )

                    ModernPasswordField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        isVisible = passwordVisible,
                        onVisibilityToggle = { passwordVisible = !passwordVisible }
                    )

                    ModernPasswordField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirmar contraseña",
                        isVisible = confirmPasswordVisible,
                        onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible }
                    )

                    // Sección Parámetros Médicos
                    SectionHeader(
                        title = "Configuración Médica",
                        icon = Icons.Default.Settings
                    )

                    // Parámetros médicos en tarjetas individuales
                    ModernSliderCard(
                        title = "Ratio",
                        value = ratio,
                        onValueChange = { ratio = it },
                        valueRange = 5f..30f,
                        unit = "g de carbohidratos",
                        description = "Gramos de carbohidratos que cubre una unidad de insulina"
                    )

                    ModernSliderCard(
                        title = "Sensibilidad",
                        value = sensitivity,
                        onValueChange = { sensitivity = it },
                        valueRange = 10f..100f,
                        unit = "mg/dL",
                        description = "Disminución de glucemia con una unidad de insulina"
                    )

                    ModernSliderCard(
                        title = "Target de Glucemia",
                        value = glycemiaTarget,
                        onValueChange = { glycemiaTarget = it },
                        valueRange = 90f..120f,
                        unit = "mg/dL objetivo",
                        description = "Nivel objetivo de glucemia a mantener"
                    )

                    // Mensaje de error moderno
                    if (errorMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Error.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
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
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botones modernos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón Volver (secundario)
                        OutlinedButton(
                            onClick = onBackToLogin,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.5.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Orange400, Orange600)
                                )
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Orange600
                            )
                        ) {
                            Text(
                                "Volver",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        // Botón Registrarse (principal)
                        Button(
                            onClick = {
                                if (password != confirmPassword) {
                                    errorMessage = "Las contraseñas no coinciden"
                                    return@Button
                                }

                                if (email.isBlank() || name.isBlank() || lastName.isBlank() || password.isBlank()) {
                                    errorMessage = "Todos los campos son obligatorios"
                                    return@Button
                                }

                                errorMessage = null
                                isLoading = true
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val service = RetrofitClient.retrofit.create(RegisterService::class.java)
                                        val response = service.register(
                                            RegisterRequest(
                                                email = email.trim(),
                                                password = password,
                                                name = name.trim(),
                                                lastName = lastName.trim(),
                                                ratio = ratio,
                                                sensitivity = sensitivity,
                                                glycemiaTarget = glycemiaTarget
                                            )
                                        )

                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            if (response.containsKey("detail") && response["detail"] != null) {
                                                errorMessage = response["detail"]
                                            } else {
                                                onRegisterSuccess()
                                            }
                                        }
                                    } catch (e: retrofit2.HttpException) {
                                        isLoading = false
                                        val errorBody = e.response()?.errorBody()?.string()
                                        val statusCode = e.code()
                                        Log.e("RegisterScreen", "HttpException: $statusCode - $errorBody", e)
                                        val userMessage = when (statusCode) {
                                            400 -> "Solicitud incorrecta. Revisa los datos."
                                            409 -> "El email ya está registrado."
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
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Orange600,
                                disabledContainerColor = Orange600.copy(alpha = 0.6f)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Crear Cuenta",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Componentes reutilizables modernos
@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Orange600,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = Gray800
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = Orange500,
                modifier = Modifier.size(20.dp)
            )
        },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Orange500,
            unfocusedBorderColor = Gray300,
            focusedLabelColor = Orange600,
            unfocusedLabelColor = Gray500,
            cursorColor = Orange600
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Orange500,
                modifier = Modifier.size(20.dp)
            )
        },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onVisibilityToggle) {
                Icon(
                    imageVector = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (isVisible) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = Orange500,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Orange500,
            unfocusedBorderColor = Gray300,
            focusedLabelColor = Orange600,
            unfocusedLabelColor = Gray500,
            cursorColor = Orange600
        )
    )
}

@Composable
private fun ModernSliderCard(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Gray50
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Gray800
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Orange500.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$value $unit",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Orange700,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = Orange600,
                    activeTrackColor = Orange500,
                    inactiveTrackColor = Orange200
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
