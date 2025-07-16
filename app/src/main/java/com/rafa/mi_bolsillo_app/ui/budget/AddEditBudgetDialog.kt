package com.rafa.mi_bolsillo_app.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rafa.mi_bolsillo_app.data.local.entity.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetDialog(
    budgetToEdit: BudgetUiItem?,
    availableCategories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (categoryId: Long, amount: Double) -> Unit
) {
    val isEditMode = budgetToEdit != null
    val title = if (isEditMode) "Editar Presupuesto" else "Nuevo Presupuesto"

    val allCategories = remember(budgetToEdit, availableCategories) {
        if (isEditMode && budgetToEdit != null) {
            listOf(budgetToEdit.category)
        } else {
            availableCategories
        }
    }

    var selectedCategory by remember { mutableStateOf(allCategories.firstOrNull()) }
    var amountString by rememberSaveable(budgetToEdit) { mutableStateOf(budgetToEdit?.budget?.amount?.toString() ?: "") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 20.dp))

                // Selector de categoría (solo para nuevos presupuestos)
                if (!isEditMode) {
                    ExposedDropdownMenuBox(
                        expanded = categoryMenuExpanded,
                        onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded },
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "",
                            onValueChange = {},
                            label = { Text("Categoría") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            isError = categoryError != null,
                        )
                        ExposedDropdownMenu(
                            expanded = categoryMenuExpanded && allCategories.isNotEmpty(),
                            onDismissRequest = { categoryMenuExpanded = false }
                        ) {
                            allCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategory = category
                                        categoryMenuExpanded = false
                                        categoryError = null
                                    }
                                )
                            }
                        }
                    }
                    if (categoryError != null) Text(categoryError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                } else {
                    OutlinedTextField(
                        value = budgetToEdit?.category?.name ?: "",
                        onValueChange = {},
                        label = { Text("Categoría") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Campo de monto
                OutlinedTextField(
                    value = amountString,
                    onValueChange = {
                        amountString = it.filter { c -> c.isDigit() || c == '.' || c == ',' }
                        amountError = null
                    },
                    label = { Text("Monto del Presupuesto") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = amountError != null,
                    supportingText = { if (amountError != null) Text(amountError!!) }
                )
                Spacer(Modifier.height(24.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val amount = amountString.replace(',', '.').toDoubleOrNull()
                        var isValid = true
                        if (selectedCategory == null) {
                            categoryError = "Selecciona una categoría"; isValid = false
                        }
                        if (amount == null || amount <= 0) {
                            amountError = "Monto inválido"; isValid = false
                        }

                        if (isValid) {
                            onConfirm(selectedCategory!!.id, amount!!)
                        }
                    }) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}