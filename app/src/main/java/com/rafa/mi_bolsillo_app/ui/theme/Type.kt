package com.rafa.mi_bolsillo_app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Archivo de tipografía personalizado para el tema oscuro/claro.
 *
 * Este archivo contiene la tipografía personalizada para el tema oscuro/claro.
 */

// Familia de fuentes principal (Roboto es generalmente FontFamily.Default en Android)
val RobotoFamily = FontFamily.Default

val AppTypography = Typography(
    // Título de Pantalla (ej. "Dashboard", "Nueva Transacción")
    titleLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Medium, // Roboto Medium
        fontSize = 20.sp
    ),
    // Número Grande Destacado (ej. Monto del Balance en Dashboard)
    headlineLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Bold, // Roboto Bold
        fontSize = 32.sp
    ),
    // Encabezado de Sección / Etiqueta Importante
    titleMedium = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Medium, // Roboto Medium
        fontSize = 16.sp
    ),
    // Texto del Cuerpo Principal
    bodyLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Normal, // Roboto Regular
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Texto Secundario / Subtexto
    bodyMedium = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Normal, // Roboto Regular
        fontSize = 14.sp
    ),
    // Texto de Botón
    labelLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Medium, // Roboto Medium
        fontSize = 14.sp,
        letterSpacing = 0.1.sp,
    ),
    // Leyenda / Texto Pequeño
    bodySmall = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Normal, // Roboto Regular
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)