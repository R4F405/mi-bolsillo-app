package com.rafa.mi_bolsillo_app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Nuevo esquema de colores para el TEMA CLARO, basado en la paleta simplificada
private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = BrandWhite,
    secondary = TextPrimaryDark, // El color de acento es el texto oscuro
    onSecondary = BrandWhite,
    background = LightBackground,
    onBackground = TextPrimaryDark,
    surface = BrandWhite,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondary,
    error = ExpenseRed,
    onError = BrandWhite,
    outline = LightGray
)

// Nuevo esquema de colores para el TEMA OSCURO, basado en la paleta simplificada
private val DarkColorScheme = darkColorScheme(
    primary = BrandGray,
    onPrimary = BrandBlack,
    secondary = LightGray, // El color del FAB en modo oscuro
    onSecondary = TextPrimaryDark,
    background = BrandBlack,
    onBackground = TextPrimaryLight,
    surface = DarkSurface,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondary,
    error = ExpenseRed,
    onError = BrandWhite,
    outline = TextSecondary
)

@Composable
fun MiBolsilloAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Material You - Desactivado por defecto para usar nuestro tema
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // La barra de estado en modo oscuro ahora usará el color de la superficie (tarjetas)
            // y en modo claro usará el color primario, tal como en tus capturas.
            val statusBarColor = if (darkTheme) {
                colorScheme.surface.toArgb()
            } else {
                colorScheme.primary.toArgb()
            }
            window.statusBarColor = statusBarColor

            // Asegura que los iconos de la barra de estado (hora, batería) tengan el contraste correcto.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // La tipografía no ha cambiado
        content = content
    )
}