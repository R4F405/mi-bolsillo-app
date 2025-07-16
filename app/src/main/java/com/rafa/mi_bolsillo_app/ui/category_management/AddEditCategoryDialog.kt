package com.rafa.mi_bolsillo_app.ui.category_management

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rafa.mi_bolsillo_app.data.local.entity.Category

// Using the same helper functions and color list from the old sheet content
val suggestedCategoryColors: List<Color> = listOf(
    Color(0xFFF44336), Color(0xFFE91E63), Color(0xFFFF5252), Color(0xFFFF4081), Color(0xFFAD1457),
    Color(0xFF9C27B0), Color(0xFF673AB7), Color(0xFF7E57C2), Color(0xFFBA68C8),
    Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF42A5F5), Color(0xFF1A237E),
    Color(0xFF00BCD4), Color(0xFF00ACC1), Color(0xFF4DD0E1), Color(0xFF006064),
    Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFF66BB6A), Color(0xFF2E7D32),
    Color(0xFFCDDC39), Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722), Color(0xFFF57C00),
    Color(0xFF795548), Color(0xFF8D6E63), Color(0xFF4E342E),
    Color(0xFF9E9E9E), Color(0xFF757575), Color(0xFF424242), Color(0xFF607D8B), Color(0xFF37474F),
    Color(0xFFFFFFFF), Color(0xFF000000)
)
fun Color.toHexString(): String {
    return String.format("#%06X", 0xFFFFFF and this.toArgb())
}

@Composable
fun AddEditCategoryDialog(
    categoryToEdit: Category?,
    onDismiss: () -> Unit,
    onConfirm: (id: Long?, name: String, colorHex: String) -> Unit
) {
    val isEditMode = categoryToEdit != null
    val title = if (isEditMode) "Editar Categoría" else "Nueva Categoría"

    var name by remember(categoryToEdit) { mutableStateOf(categoryToEdit?.name ?: "") }
    var selectedColorHex by remember(categoryToEdit) {
        mutableStateOf(categoryToEdit?.colorHex ?: suggestedCategoryColors.first().toHexString())
    }
    var nameError by remember { mutableStateOf<String?>(null) }
    var colorError by remember { mutableStateOf<String?>(null) } // Although not used for validation, good practice to keep it.

    LaunchedEffect(categoryToEdit) {
        name = categoryToEdit?.name ?: ""
        selectedColorHex = categoryToEdit?.colorHex ?: suggestedCategoryColors.first().toHexString()
        nameError = null
        colorError = null
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Nombre de la categoría") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    isError = nameError != null,
                    supportingText = { if (nameError != null) Text(nameError!!) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Color de la Categoría", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestedCategoryColors) { color ->
                        val colorString = color.toHexString()
                        ColorPickerItem(
                            color = color,
                            isSelected = selectedColorHex == colorString,
                            onClick = { selectedColorHex = colorString; colorError = null }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (name.isBlank()) {
                            nameError = "El nombre es obligatorio"
                        } else {
                            onConfirm(categoryToEdit?.id, name, selectedColorHex)
                        }
                    }) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPickerItem(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(
                    BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                    CircleShape
                ) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            val checkColor = if (color.luminance() > 0.5) Color.Black else Color.White
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Color seleccionado",
                tint = checkColor
            )
        }
    }
}

fun Color.luminance(): Float {
    val red = this.red
    val green = this.green
    val blue = this.blue
    return (0.2126f * red + 0.7152f * green + 0.0722f * blue)
}