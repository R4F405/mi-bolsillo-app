package com.rafa.mi_bolsillo_app.ui.category_management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding // Para ajustar por el teclado
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.ui.theme.MiBolsilloAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategorySheetContent(
    categoryToEdit: Category?, // Null si es para añadir una nueva
    onSave: (id: Long?, name: String, colorHex: String, iconName: String) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    var name by remember(categoryToEdit) { mutableStateOf(categoryToEdit?.name ?: "") }
    var colorHex by remember(categoryToEdit) { mutableStateOf(categoryToEdit?.colorHex ?: "#") } // Iniciar con # para guiar
    var iconName by remember(categoryToEdit) { mutableStateOf(categoryToEdit?.iconName ?: "ic_label") } // Icono por defecto

    var nameError by remember { mutableStateOf<String?>(null) }
    var colorError by remember { mutableStateOf<String?>(null) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val title = if (categoryToEdit == null) "Nueva Categoría" else "Editar Categoría"

    // Cargar datos cuando categoryToEdit cambia y el sheet es visible (o está a punto de serlo)
    // Esto es útil si el mismo sheet se reutiliza y el categoryToEdit cambia mientras está abierto (menos común).
    // El `key` en `remember` ya maneja la inicialización, pero esto puede ser un re-trigger.
    LaunchedEffect(categoryToEdit) {
        name = categoryToEdit?.name ?: ""
        colorHex = categoryToEdit?.colorHex ?: "#"
        iconName = categoryToEdit?.iconName ?: "ic_label"
        nameError = null
        colorError = null
    }


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.imePadding() // Ajusta el padding cuando el teclado aparece
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()), // Para contenido más largo que el sheet
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text("Nombre de la categoría") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                isError = nameError != null,
                supportingText = { if (nameError != null) Text(nameError!!) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = colorHex,
                onValueChange = {
                    // Permitir que el usuario escriba libremente, validación al guardar
                    colorHex = if (it.startsWith("#")) it else "#$it"
                    colorError = null
                },
                label = { Text("Color Hex (ej. #RRGGBB)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                isError = colorError != null,
                supportingText = { if (colorError != null) Text(colorError!!) }
                // TODO: Añadir un selector de color visual en el futuro
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = iconName,
                onValueChange = { iconName = it },
                label = { Text("Nombre del Icono (ej. ic_nombre_icono)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                // TODO: Añadir un selector de iconos visual en el futuro
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    var isValid = true
                    if (name.isBlank()) {
                        nameError = "El nombre es obligatorio"
                        isValid = false
                    }
                    // Validación básica de color hexadecimal
                    val hexPattern = Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
                    if (!hexPattern.matches(colorHex)) {
                        colorError = "Formato de color inválido (ej. #RRGGBB)"
                        isValid = false
                    }

                    if (isValid) {
                        onSave(categoryToEdit?.id, name, colorHex, iconName)
                        // El ViewModel se encargará de cerrar el sheet a través del uiState o el onDismiss puede ser llamado desde el ViewModel.
                        // Por ahora, onSave no cierra directamente, el ViewModel lo hará.
                    }
                }) {
                    Text("Guardar")
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Espacio para que el contenido no quede pegado abajo
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun AddCategorySheetContentPreview() {
    MiBolsilloAppTheme {
        val sheetState = rememberModalBottomSheetState()
        AddEditCategorySheetContent(
            categoryToEdit = null,
            onSave = { _, _, _, _ -> },
            onDismiss = {},
            sheetState = sheetState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun EditCategorySheetContentPreview() {
    MiBolsilloAppTheme {
        val sheetState = rememberModalBottomSheetState()
        AddEditCategorySheetContent(
            categoryToEdit = Category(1, "Compras", "#FF5722", "ic_shopping_cart", false),
            onSave = { _, _, _, _ -> },
            onDismiss = {},
            sheetState = sheetState
        )
    }
}