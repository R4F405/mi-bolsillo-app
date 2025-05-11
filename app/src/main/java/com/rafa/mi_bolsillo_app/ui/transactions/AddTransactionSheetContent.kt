package com.rafa.mi_bolsillo_app.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.HorizontalDivider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.ui.theme.MiBolsilloAppTheme // Para el Preview
import androidx.compose.ui.tooling.preview.Preview // Para el Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheetContent(
    viewModel: TransactionViewModel,
    onTransactionAdded: () -> Unit
) {
    var concepto by rememberSaveable { mutableStateOf("") } // Cambiado de description a concepto
    var amount by rememberSaveable { mutableStateOf("") }
    var selectedTransactionType by remember { mutableStateOf(TransactionType.EXPENSE) }

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryError by rememberSaveable { mutableStateOf<String?>(null) }

    // Para depuración:
    // Text("Categorías en el selector: ${categories.size}")

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 24.dp) // Aumentado padding vertical
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Nueva Transacción",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp) // Aumentado padding inferior
        )

        // Selector de Tipo (Ingreso/Gasto) - Mantenido al principio
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TransactionType.values().forEach { type ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clickable { selectedTransactionType = type }
                ) {
                    RadioButton(
                        selected = (type == selectedTransactionType),
                        onClick = { selectedTransactionType = type }
                    )
                    Text(
                        text = if (type == TransactionType.INCOME) "Ingreso" else "Gasto",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) // Divisor visual

        // Campo de Concepto (antes Descripción)
        OutlinedTextField(
            value = concepto,
            onValueChange = { concepto = it },
            label = { Text("Concepto (Opcional)") }, // Cambiado el label
            singleLine = false,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
        Spacer(modifier = Modifier.height(12.dp)) // Aumentado spacer

        // Campo de Monto
        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it
                amountError = null
            },
            label = { Text("Monto") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = amountError != null,
            supportingText = { if (amountError != null) Text(amountError!!) }
        )
        Spacer(modifier = Modifier.height(12.dp)) // Aumentado spacer

        // Selector de Categoría
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "Seleccionar categoría",
                onValueChange = { /* No editable directamente */ },
                label = { Text("Categoría") },
                readOnly = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "Abrir categorías") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        println("Categorías disponibles para el menú: ${categories.joinToString { it.name }}") // Log para depurar
                        if (categories.isNotEmpty()) { // Solo expandir si hay categorías
                            categoryMenuExpanded = true
                        } else {
                            categoryError = "No hay categorías disponibles" // Mensaje si no hay
                        }
                    },
                isError = categoryError != null,
                supportingText = { if (categoryError != null) Text(categoryError!!) }
            )
            DropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = { categoryMenuExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f) // Ajusta el ancho del menú desplegable
            ) {
                if (categories.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay categorías") },
                        onClick = { categoryMenuExpanded = false },
                        enabled = false // Deshabilitado si no hay nada que seleccionar
                    )
                } else {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategory = category
                                categoryMenuExpanded = false
                                categoryError = null
                                println("Categoría seleccionada: ${category.name}") // Log para depurar
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp)) // Aumentado spacer

        // Botón de Guardar
        Button(
            onClick = {
                val amountDouble = amount.toDoubleOrNull()
                var isValid = true
                amountError = null // Resetear errores
                categoryError = null // Resetear errores

                if (amountDouble == null || amountDouble <= 0) {
                    amountError = "Monto inválido"
                    isValid = false
                }
                if (selectedCategory == null) {
                    categoryError = "Selecciona una categoría"
                    isValid = false
                }

                if (isValid && amountDouble != null && selectedCategory != null) {
                    viewModel.addTransaction(
                        amount = amountDouble,
                        date = System.currentTimeMillis(),
                        concepto = concepto.takeIf { it.isNotBlank() }, // Usa 'concepto'
                        categoryId = selectedCategory!!.id,
                        type = selectedTransactionType
                    )
                    onTransactionAdded()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp) // Padding para botón más alto
        ) {
            Text("Guardar Transacción", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}