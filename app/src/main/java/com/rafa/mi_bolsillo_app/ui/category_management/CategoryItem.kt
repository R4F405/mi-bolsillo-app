package com.rafa.mi_bolsillo_app.ui.category_management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.ui.theme.MiBolsilloAppTheme

/**
 * Composable para mostrar un item de categoría.
 *
 * Muestra el color, el nombre y un ícono de edición y eliminación.
 */

@Composable
fun CategoryItem(
    category: Category,
    onEditClick: (Category) -> Unit,
    onDeleteClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp), // Reducido de 4.dp
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp), // Reducido de 12.dp
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                CategoryColorIndicator(hexColor = category.colorHex)
                Spacer(modifier = Modifier.width(10.dp)) // Reducido de 12.dp
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Íconos de edición y eliminación
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onEditClick(category) }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Editar categoría",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { onDeleteClick(category) }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar categoría",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Composable para mostrar un indicador de color de categoría
@Composable
fun CategoryColorIndicator(hexColor: String, modifier: Modifier = Modifier) {
    val color = try {
        Color(android.graphics.Color.parseColor(hexColor))
    } catch (e: IllegalArgumentException) {
        MaterialTheme.colorScheme.surfaceVariant // Color por defecto
    }
    Box(
        modifier = modifier
            .size(18.dp) // Reducido de 20.dp
            .clip(CircleShape)
            .background(color)
    )
}

// Vista previa
@Preview(showBackground = true, name = "Custom Category Item")
@Composable
fun CategoryItemCustomPreview() {
    MiBolsilloAppTheme {
        CategoryItem(
            category = Category(id = 1, name = "Gimnasio", colorHex = "#3F51B5", isPredefined = false),
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

// Vista previa
@Preview(showBackground = true, name = "Predefined Category Item")
@Composable
fun CategoryItemPredefinedPreview() {
    MiBolsilloAppTheme {
        CategoryItem(
            category = Category(id = 2, name = "Comida", colorHex = "#FFC107", isPredefined = true),
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}