package com.rafa.mi_bolsillo_app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafa.mi_bolsillo_app.ui.settings.SettingsViewModel
import com.rafa.mi_bolsillo_app.ui.settings.theme.ThemeOption

/**
 * Tema de la aplicación MiBolsillo.
 * Define los esquemas de colores y la tipografía utilizados en la aplicación.
 * Este tema se adapta a los modos claro y oscuro, y permite personalizar el tema según la configuración del usuario.
 *
 */

// Esquema de colores para el TEMA CLARO, basado en una paleta simplificada
private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = BrandWhite,
    secondary = TextPrimaryDark,
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

// Esquema de colores para el TEMA OSCURO, basado en una paleta simplificada
private val DarkColorScheme = darkColorScheme(
    primary = BrandGray,
    onPrimary = BrandBlack,
    secondary = LightGray,
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

val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun MiBolsilloAppTheme(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val themeOption by settingsViewModel.currentTheme.collectAsState()

    val darkTheme = when (themeOption) {
        ThemeOption.LIGHT -> false
        ThemeOption.DARK -> true
        ThemeOption.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val statusBarColor = if (darkTheme) {
                colorScheme.surface.toArgb()
            } else {
                colorScheme.primary.toArgb()
            }
            window.statusBarColor = statusBarColor
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}