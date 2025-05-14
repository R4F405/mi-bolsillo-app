package com.rafa.mi_bolsillo_app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Archivo de colores personalizados para el tema oscuro/claro.
 *
 * Este archivo contiene colores personalizados para el tema oscuro/claro.
 */

// Tus colores definidos
val AppPrimary = Color(0xFF073763)
val AppAccent = Color(0xFF121212)
val AppBackground = Color(0xFFFAFAFA)
val AppCardBackground = Color(0xFFFFFFFF) // Surface color
val AppTextPrimary = Color(0xFF212121)
val AppTextSecondary = Color(0xFF757575)
val AppBorders = Color(0xFFE0E0E0)
val AppIncome = Color(0xFF4CAF50)
val AppExpense = Color(0xFFD32F2F) // También usado para 'error'
val AppTextOnPrimary = Color(0xFFFFFFFF)

// ---- Light Theme Material 3 Colors ----
val md_theme_light_primary = AppPrimary
val md_theme_light_onPrimary = AppTextOnPrimary
val md_theme_light_primaryContainer = Color(0xFFDCE1FF) // Un azul muy claro, derivado
val md_theme_light_onPrimaryContainer = Color(0xFF001550) // Texto oscuro sobre el container claro

val md_theme_light_secondary = AppAccent // #121212 - para elementos de acción clave.
val md_theme_light_onSecondary = AppTextOnPrimary // #FFFFFF - texto sobre el acento oscuro
val md_theme_light_secondaryContainer = Color(0xFFE0E0E0) // Usando AppBorders como container
val md_theme_light_onSecondaryContainer = AppTextPrimary // #212121 texto sobre container secundario

val md_theme_light_tertiary = Color(0xFF006A60) // Un verde azulado como ejemplo, puedes ajustarlo
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFF74F8E5)
val md_theme_light_onTertiaryContainer = Color(0xFF00201C)

val md_theme_light_error = AppExpense // #D32F2F
val md_theme_light_onError = AppTextOnPrimary // #FFFFFF
val md_theme_light_errorContainer = Color(0xFFFFDAD6) // Rojo muy claro
val md_theme_light_onErrorContainer = Color(0xFF410002) // Texto oscuro sobre errorContainer

val md_theme_light_background = AppBackground // #FAFAFA
val md_theme_light_onBackground = AppTextPrimary // #212121
val md_theme_light_surface = AppCardBackground // #FFFFFF
val md_theme_light_onSurface = AppTextPrimary // #212121

val md_theme_light_surfaceVariant = AppCardBackground
val md_theme_light_onSurfaceVariant = AppTextSecondary // #757575
val md_theme_light_outline = AppBorders // #E0E0E0
val md_theme_light_outlineVariant = Color(0xFFCFCFCF) // Un poco más oscuro que AppBorders

// ---- Dark Theme Material 3 Colors ----
val md_theme_dark_primary = Color(0xFFB0C6FF) // Primario más claro para tema oscuro
val md_theme_dark_onPrimary = Color(0xFF002778) // Texto oscuro sobre primario claro
val md_theme_dark_primaryContainer = AppPrimary // Usando primario original como container oscuro
val md_theme_dark_onPrimaryContainer = Color(0xFFD9E2FF)

val md_theme_dark_secondary = Color(0xFFBDBDBD) // Un gris claro como secundario
val md_theme_dark_onSecondary = AppAccent // #121212 - texto oscuro sobre secundario claro
val md_theme_dark_secondaryContainer = Color(0xFF333333) // Gris oscuro para container
val md_theme_dark_onSecondaryContainer = Color(0xFFE0E0E0)

val md_theme_dark_tertiary = Color(0xFF54DBC9) // Verde azulado más claro
val md_theme_dark_onTertiary = Color(0xFF003731)
val md_theme_dark_tertiaryContainer = Color(0xFF005048)
val md_theme_dark_onTertiaryContainer = Color(0xFF74F8E5)

val md_theme_dark_error = Color(0xFFFFB4AB) // Error más claro
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = AppExpense // Usando color de gasto original como container
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

val md_theme_dark_background = AppAccent // #121212 - acento como fondo oscuro
val md_theme_dark_onBackground = Color(0xFFE0E0E0) // Bordes
val md_theme_dark_surface = Color(0xFF1E1E1E) // Superficie un poco más clara que el fondo
val md_theme_dark_onSurface = Color(0xFFE0E0E0)

val md_theme_dark_surfaceVariant = Color(0xFF303030) // Variante de superficie
val md_theme_dark_onSurfaceVariant = AppTextSecondary // #757575
val md_theme_dark_outline = AppTextSecondary // #757575
val md_theme_dark_outlineVariant = Color(0xFF505050)