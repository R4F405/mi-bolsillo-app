package com.rafa.mi_bolsillo_app.ui.category_management

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.ui.theme.MiBolsilloAppTheme

/**
 * Composable para la pantalla de añadir o editar una categoría.
 *
 * Permite al usuario añadir o editar una categoría existente.
 */

// Lista de colores sugeridos
val suggestedCategoryColors: List<Color> = listOf(
    // Rojos y Rosados
    Color(0xFFF44336), // Rojo
    Color(0xFFE91E63), // Rosa
    Color(0xFFFF5252), // Rojo Claro
    Color(0xFFFF4081), // Rosa Acento
    Color(0xFFAD1457), // Rosa Oscuro (Cranberry)

    // Púrpuras y Violetas
    Color(0xFF9C27B0), // Púrpura
    Color(0xFF673AB7), // Púrpura Intenso
    Color(0xFF7E57C2), // Lavanda
    Color(0xFFBA68C8), // Orquídea Pálido

    // Azules
    Color(0xFF3F51B5), // Indigo
    Color(0xFF2196F3), // Azul
    Color(0xFF03A9F4), // Azul Claro
    Color(0xFF42A5F5), // Azul Cielo
    Color(0xFF1A237E), // Azul Marino (Indigo Oscuro)

    // Cianes y Turquesas
    Color(0xFF00BCD4), // Cian
    Color(0xFF00ACC1), // Turquesa
    Color(0xFF4DD0E1), // Turquesa Claro
    Color(0xFF006064), // Cian Oscuro

    // Verdes
    Color(0xFF009688), // Teal (Verde Azulado)
    Color(0xFF4CAF50), // Verde
    Color(0xFF8BC34A), // Verde Lima Claro
    Color(0xFF66BB6A), // Verde Medio
    Color(0xFF2E7D32), // Verde Oscuro (Bosque)

    // Amarillos y Naranjas
    Color(0xFFCDDC39), // Lima
    Color(0xFFFFEB3B), // Amarillo
    Color(0xFFFFC107), // Ámbar (Amarillo Naranja)
    Color(0xFFFF9800), // Naranja
    Color(0xFFFF5722), // Naranja Intenso (Coral)
    Color(0xFFF57C00), // Naranja Oscuro

    // Marrones y Grises
    Color(0xFF795548), // Marrón
    Color(0xFF8D6E63), // Marrón Claro (Beige)
    Color(0xFF4E342E), // Marrón Oscuro (Café)
    Color(0xFF9E9E9E), // Gris
    Color(0xFF757575), // Gris Medio
    Color(0xFF424242), // Gris Oscuro
    Color(0xFF607D8B), // Gris Azulado
    Color(0xFF37474F), // Pizarra Oscuro

    // Negros y Blancos
     Color(0xFFFFFFFF), // Blanco
    Color(0xFF000000)  // Negro
)

// Función para convertir Color de Compose a String Hexadecimal (ej. #RRGGBB)
fun Color.toHexString(): String {
    return String.format("#%06X", 0xFFFFFF and this.toArgb())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategorySheetContent(
    categoryToEdit: Category?,
    onSave: (id: Long?, name: String, colorHex: String, iconName: String) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    var name by remember(categoryToEdit) { mutableStateOf(categoryToEdit?.name ?: "") }
    var selectedColorHex by remember(categoryToEdit) {
        mutableStateOf(categoryToEdit?.colorHex ?: suggestedCategoryColors.first().toHexString())
    }
    var iconName by remember(categoryToEdit) { mutableStateOf(categoryToEdit?.iconName ?: "ic_label") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var colorError by remember { mutableStateOf<String?>(null) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val title = if (categoryToEdit == null) "Nueva Categoría" else "Editar Categoría"

    // Actualizar los campos con los valores de la categoría a editar
    LaunchedEffect(categoryToEdit) {
        name = categoryToEdit?.name ?: ""
        selectedColorHex = categoryToEdit?.colorHex ?: suggestedCategoryColors.first().toHexString()
        iconName = categoryToEdit?.iconName ?: "ic_label"
        nameError = null
        colorError = null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

            // --- NOMBRE DE LA CATEGORÍA ---
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

            // --- SELECTOR DE COLOR VISUAL ---
            Text("Color de la Categoría", style = MaterialTheme.typography.labelLarge, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
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
            Text(
                text = "Seleccionado: $selectedColorHex",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
            if (colorError != null) {
                Text(colorError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- NOMBRE DEL ICONO ---
            OutlinedTextField(
                value = iconName,
                onValueChange = { iconName = it },
                label = { Text("Nombre del Icono (ej. ic_nombre_icono)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Botones de cancelar y guardar
                TextButton(onClick = onDismiss) { Text("Cancelar") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    var isValid = true
                    if (name.isBlank()) {
                        nameError = "El nombre es obligatorio"
                        isValid = false
                    }

                    if (isValid) {
                        onSave(categoryToEdit?.id, name, selectedColorHex, iconName)
                    }
                }) {
                    Text("Guardar")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Composable para un item de la fila de colores
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
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Color seleccionado",
                tint = if (color.luminance() > 0.5) Color.Black else Color.White // Contraste para el check
            )
        }
    }
}

// Helper para calcular la luminancia y decidir el color del check (opcional pero mejora UX)
fun Color.luminance(): Float {
    val red = this.red
    val green = this.green
    val blue = this.blue
    return (0.2126f * red + 0.7152f * green + 0.0722f * blue)
}

// Vista previa
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun AddCategorySheetContentWithColorPickerPreview() {
    MiBolsilloAppTheme {
        val sheetState = rememberModalBottomSheetState()
        Box(Modifier.fillMaxSize().padding(top=100.dp)){ // Para que el sheet no ocupe toda la preview
            AddEditCategorySheetContent(
                categoryToEdit = null,
                onSave = { _, _, _, _ -> },
                onDismiss = {},
                sheetState = sheetState
            )
        }
    }
}