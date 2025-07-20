package com.rafa.mi_bolsillo_app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.rafa.mi_bolsillo_app.ui.theme.MiBolsilloAppTheme

/**
 * Composable para mostrar un diálogo de confirmación.
 * Permite al usuario confirmar o cancelar una acción.
 *
 * @param showDialog Indica si el diálogo debe mostrarse.
 * @param onConfirm Acción a realizar al confirmar.
 * @param onDismiss Acción a realizar al cancelar.
 * @param title Título del diálogo.
 * @param message Mensaje del diálogo.
 * @param confirmButtonText Texto del botón de confirmación.
 * @param dismissButtonText Texto del botón de cancelación.
 * @param icon Icono opcional para mostrar en el diálogo.
 */

@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmButtonText: String = "Confirmar",
    dismissButtonText: String = "Cancelar",
    icon: ImageVector? = Icons.Filled.Warning
) {
    // Composición del diálogo de confirmación
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = icon?.let {
                { Icon(it, contentDescription = null) }
            },
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissButtonText)
                }
            }
        )
    }
}

// Vista previa del diálogo de confirmación
@Preview(showBackground = true)
@Composable
fun ConfirmationDialogPreview() {
    MiBolsilloAppTheme {
        ConfirmationDialog(
            showDialog = true,
            onConfirm = {},
            onDismiss = {},
            title = "Confirmar Acción",
            message = "¿Estás seguro de que quieres realizar esta acción?"
        )
    }
}