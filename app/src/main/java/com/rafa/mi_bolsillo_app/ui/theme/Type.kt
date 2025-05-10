package com.rafa.mi_bolsillo_app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Familia de fuentes principal (Roboto es generalmente FontFamily.Default en Android)
val RobotoFamily = FontFamily.Default // O podrías definir FontFamily.SansSerif

val AppTypography = Typography(
    // Título de Pantalla (ej. "Dashboard", "Nueva Transacción")
    titleLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Medium, // Roboto Medium
        fontSize = 20.sp
        // El color vendrá del ColorScheme (onBackground o onSurface)
    ),
    // Número Grande Destacado (ej. Monto del Balance en Dashboard)
    headlineLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Bold, // Roboto Bold (o SemiBold si es demasiado)
        fontSize = 32.sp
        // El color vendrá del ColorScheme
    ),
    // Encabezado de Sección / Etiqueta Importante
    titleMedium = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Medium, // Roboto Medium
        fontSize = 16.sp
        // El color puede ser onBackground, onSurface, o primary si se aplica específicamente
    ),
    // Texto del Cuerpo Principal
    bodyLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Normal, // Roboto Regular
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
        // El color vendrá del ColorScheme
    ),
    // Texto Secundario / Subtexto
    bodyMedium = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Normal, // Roboto Regular
        fontSize = 14.sp
        // El color vendrá del ColorScheme (onSurfaceVariant)
    ),
    // Texto de Botón
    labelLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Medium, // Roboto Medium
        fontSize = 14.sp,
        letterSpacing = 0.1.sp,
        // El color vendrá del ColorScheme (onPrimary, onSecondary, o primary para texto)
    ),
    // Leyenda / Texto Pequeño
    bodySmall = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Normal, // Roboto Regular
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
        // El color vendrá del ColorScheme (onSurfaceVariant)
    )
)