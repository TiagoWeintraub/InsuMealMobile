package com.insumeal.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Orange300,
    secondary = Orange200,
    tertiary = SoftOrange,
    background = Gray900,
    surface = Gray800,
    onPrimary = Gray900,
    onSecondary = Gray900,
    onTertiary = Gray900,
    onBackground = Gray100,
    onSurface = Gray100
)

private val LightColorScheme = lightColorScheme(
    primary = Orange500, // Naranja principal
    secondary = Orange600, // Naranja complementario
    tertiary = SoftOrange, // Naranja suave para acentos
    primaryContainer = Color.White, // Contenedor blanco puro
    secondaryContainer = Color.White, // Contenedor blanco puro
    tertiaryContainer = Color.White, // Contenedor blanco puro
    surface = Color.White, // Superficie blanca pura
    surfaceVariant = Color.White, // Variante de superficie blanca
    background = Color.White, // Fondo blanco puro
    onPrimary = Color.White, // Texto sobre naranja
    onSecondary = Color.White, // Texto sobre naranja secundario
    onTertiary = Gray900, // Texto sobre naranja suave
    onPrimaryContainer = Gray900, // Texto sobre contenedor blanco
    onSecondaryContainer = Gray900, // Texto sobre contenedor blanco
    onTertiaryContainer = Gray900, // Texto sobre contenedor blanco
    onBackground = OnSurface, // Texto sobre fondo
    onSurface = OnSurface, // Texto sobre superficie
    onSurfaceVariant = OnSurfaceVariant, // Texto sobre variante de superficie
    outline = Gray400, // Líneas y bordes
    outlineVariant = Gray300, // Variante de líneas
)

@Composable
fun InsuMealTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color está disponible en Android 12+
    dynamicColor: Boolean = false, // Cambiar a false para forzar el uso de nuestros colores
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
