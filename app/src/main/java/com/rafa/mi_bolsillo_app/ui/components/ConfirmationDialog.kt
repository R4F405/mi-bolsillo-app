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

@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmButtonText: String = "Confirmar",
    dismissButtonText: String = "Cancelar",
    icon: ImageVector? = Icons.Filled.Warning // Icono opcional
) {
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