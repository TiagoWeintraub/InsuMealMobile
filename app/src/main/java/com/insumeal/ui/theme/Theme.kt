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
    primary = Turquoise300,
    secondary = Turquoise200,
    tertiary = SoftTurquoise,
    background = Gray900,
    surface = Gray800,
    onPrimary = Gray900,
    onSecondary = Gray900,
    onTertiary = Gray900,
    onBackground = Gray100,
    onSurface = Gray100
)

private val LightColorScheme = lightColorScheme(
    primary = Turquoise500, // Turquesa principal
    secondary = Turquoise600, // Turquesa complementario
    tertiary = SoftTurquoise, // Turquesa suave para acentos
    primaryContainer = Color(0xFFFFFFFF), // Contenedor blanco puro con hex
    secondaryContainer = Color(0xFFFFFFFF), // Contenedor blanco puro con hex
    tertiaryContainer = Color(0xFFFFFFFF), // Contenedor blanco puro con hex
    surface = Color(0xFFFFFFFF), // Superficie blanca pura con hex
    surfaceVariant = Color(0xFFFFFFFF), // Variante de superficie blanca con hex
    surfaceContainer = Color(0xFFFFFFFF), // Contenedor de superficie blanco puro
    surfaceContainerHigh = Color(0xFFFFFFFF), // Contenedor alto blanco puro
    surfaceContainerHighest = Color(0xFFFFFFFF), // Contenedor más alto blanco puro
    surfaceContainerLow = Color(0xFFFFFFFF), // Contenedor bajo blanco puro
    surfaceContainerLowest = Color(0xFFFFFFFF), // Contenedor más bajo blanco puro
    surfaceTint = Color(0xFFFFFFFF), // Tinte de superficie blanco puro - CLAVE!
    background = Color(0xFFFFFFFF), // Fondo blanco puro con hex
    onPrimary = Color.White, // Texto sobre turquesa
    onSecondary = Color.White, // Texto sobre turquesa secundario
    onTertiary = Gray900, // Texto sobre turquesa suave
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
    dynamicColor: Boolean = false, // Siempre false para usar nuestros colores
    content: @Composable () -> Unit
) {
    // Forzar siempre el uso de nuestros colores personalizados - siempre modo claro
    val colorScheme = LightColorScheme // Siempre usar esquema claro

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
