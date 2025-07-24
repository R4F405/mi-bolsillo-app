package com.rafa.mi_bolsillo_app.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rafa.mi_bolsillo_app.ui.settings.theme.ThemeOption

/**
 * Diálogo para seleccionar el tema de la aplicación.
 * Permite al usuario elegir entre diferentes opciones de tema.
 *
 * @param currentTheme El tema actual seleccionado.
 * @param onDismiss Función que se llama al cerrar el diálogo.
 * @param onThemeSelected Función que se llama cuando se selecciona un nuevo tema.
 *
 */

@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeOption,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeOption) -> Unit
) {
    // Diálogo para seleccionar el tema de la aplicación
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Tema") },
        text = {
            // Contenido del diálogo con opciones de tema
            Column {
                ThemeOption.values().forEach { theme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(theme.toDisplayString())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

// Extensión para convertir ThemeOption a una cadena de texto para mostrar
fun ThemeOption.toDisplayString(): String {
    return when (this) {
        ThemeOption.LIGHT -> "Claro"
        ThemeOption.DARK -> "Oscuro"
        ThemeOption.SYSTEM -> "Predeterminado del sistema"
    }
}