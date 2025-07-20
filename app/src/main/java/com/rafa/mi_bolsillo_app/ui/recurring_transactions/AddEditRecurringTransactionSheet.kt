package com.rafa.mi_bolsillo_app.ui.recurring_transactions

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.RecurrenceFrequency
import com.rafa.mi_bolsillo_app.data.local.entity.RecurringTransaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Composable para mostrar un BottomSheet que permite agregar o editar una transacción recurrente.
 * Permite al usuario ingresar detalles como nombre, monto, categoría, frecuencia, etc.
 *
 * @param sheetState Estado del BottomSheet
 * @param categories Lista de categorías disponibles
 * @param recurringTransactionToEdit Transacción recurrente a editar (null si es nueva)
 * @param onSave Callback para guardar la transacción recurrente
 * @param onDismiss Callback para cerrar el BottomSheet
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringTransactionSheet(
    sheetState: SheetState,
    categories: List<Category>,
    recurringTransactionToEdit: RecurringTransaction?,
    onSave: (
        id: Long?,
        name: String,
        amount: Double,
        description: String?,
        categoryId: Long,
        transactionType: TransactionType,
        startDate: Long,
        frequency: RecurrenceFrequency,
        interval: Int,
        dayOfMonth: Int?,
        monthOfYear: Int?,
        endDate: Long?,
        isActive: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditMode = recurringTransactionToEdit != null
    val title = if (isEditMode) "Editar Plantilla Recurrente" else "Nueva Plantilla Recurrente"

    var name by rememberSaveable(recurringTransactionToEdit) { mutableStateOf(recurringTransactionToEdit?.name ?: "") }
    var amountString by rememberSaveable(recurringTransactionToEdit) { mutableStateOf(recurringTransactionToEdit?.amount?.toString() ?: "") }
    var description by rememberSaveable(recurringTransactionToEdit) { mutableStateOf(recurringTransactionToEdit?.description ?: "") }
    var selectedTransactionType by remember(recurringTransactionToEdit) { mutableStateOf(recurringTransactionToEdit?.transactionType ?: TransactionType.EXPENSE) }

    var selectedCategory by remember(recurringTransactionToEdit, categories) {
        mutableStateOf(categories.find { it.id == recurringTransactionToEdit?.categoryId })
    }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    val initialStartDate = recurringTransactionToEdit?.startDate ?: System.currentTimeMillis()
    var startDateMillis by rememberSaveable(recurringTransactionToEdit) { mutableStateOf(initialStartDate) }
    var showStartDatePicker by remember { mutableStateOf(false) }

    var selectedFrequency by remember(recurringTransactionToEdit) { mutableStateOf(recurringTransactionToEdit?.frequency ?: RecurrenceFrequency.MONTHLY) }
    var frequencyMenuExpanded by remember { mutableStateOf(false) }
    var intervalString by rememberSaveable(recurringTransactionToEdit) { mutableStateOf(recurringTransactionToEdit?.interval?.toString() ?: "1") }
    var dayOfMonthString by rememberSaveable(recurringTransactionToEdit) { mutableStateOf(recurringTransactionToEdit?.dayOfMonth?.toString() ?: "") }
    var monthOfYearString by rememberSaveable(recurringTransactionToEdit) { mutableStateOf(recurringTransactionToEdit?.monthOfYear?.let { (it + 1).toString() } ?: "") } // +1 para display 1-12

    var endDateMillis by rememberSaveable(recurringTransactionToEdit) { mutableStateOf(recurringTransactionToEdit?.endDate) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var hasEndDate by rememberSaveable(recurringTransactionToEdit) { mutableStateOf(recurringTransactionToEdit?.endDate != null) }


    var isActive by rememberSaveable(recurringTransactionToEdit) { mutableStateOf(recurringTransactionToEdit?.isActive ?: true) }

    // Errores
    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var intervalError by remember { mutableStateOf<String?>(null) }
    var dayOfMonthError by remember { mutableStateOf<String?>(null) }
    var monthOfYearError by remember { mutableStateOf<String?>(null) }


    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.imePadding() // Para que el teclado no oculte los campos
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()) // Habilitar scroll
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 20.dp).align(Alignment.CenterHorizontally))

            // Nombre de la plantilla
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text("Nombre de la Plantilla (Ej: Netflix)") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            if (nameError != null) Text(nameError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(12.dp))

            // Tipo de Transacción (Ingreso/Gasto)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TransactionType.values().forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedTransactionType = type }.padding(horizontal = 8.dp)) {
                        RadioButton(selected = (type == selectedTransactionType), onClick = { selectedTransactionType = type })
                        Text(if (type == TransactionType.INCOME) "Ingreso" else "Gasto", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Monto
            OutlinedTextField(
                value = amountString,
                onValueChange = { amountString = it; amountError = null },
                label = { Text("Monto") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = amountError != null,
                singleLine = true
            )
            if (amountError != null) Text(amountError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(12.dp))

            // Descripción (Opcional)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Categoría
            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "",
                    onValueChange = {},
                    label = { Text("Categoría") },
                    placeholder = { Text(if (categories.isEmpty()) "No hay categorías" else "Seleccionar categoría") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    isError = categoryError != null
                )
                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded && categories.isNotEmpty(),
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    categories.forEach { category ->
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
            Spacer(modifier = Modifier.height(16.dp))

            Text("Configuración de Recurrencia", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

            // Fecha de Inicio
            DateField("Fecha de Inicio", startDateMillis, dateFormatter) { showStartDatePicker = true }
            Spacer(modifier = Modifier.height(12.dp))

            // Frecuencia
            ExposedDropdownMenuBox(
                expanded = frequencyMenuExpanded,
                onExpandedChange = { frequencyMenuExpanded = !frequencyMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedFrequency.toDisplayString(),
                    onValueChange = {},
                    label = { Text("Frecuencia") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = frequencyMenuExpanded,
                    onDismissRequest = { frequencyMenuExpanded = false }
                ) {
                    RecurrenceFrequency.values().forEach { freq ->
                        DropdownMenuItem(
                            text = { Text(freq.toDisplayString()) },
                            onClick = {
                                selectedFrequency = freq
                                frequencyMenuExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Intervalo
            OutlinedTextField(
                value = intervalString,
                onValueChange = { intervalString = it.filter { char -> char.isDigit() }; intervalError = null },
                label = { Text("Repetir cada (Intervalo)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = intervalError != null,
                singleLine = true,
                supportingText = { Text("Ej: 1 para cada ${selectedFrequency.toUnitString(1)}, 2 para cada dos ${selectedFrequency.toUnitString(2)}") }
            )
            if (intervalError != null) Text(intervalError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(12.dp))

            // Día del Mes (solo si es Mensual o Anual)
            if (selectedFrequency == RecurrenceFrequency.MONTHLY || selectedFrequency == RecurrenceFrequency.YEARLY) {
                OutlinedTextField(
                    value = dayOfMonthString,
                    onValueChange = { dayOfMonthString = it.filter { char -> char.isDigit() }; dayOfMonthError = null },
                    label = { Text("Día del Mes (1-31, opcional)") },
                    placeholder = { Text("Ej: 15")},
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = dayOfMonthError != null,
                    singleLine = true
                )
                if (dayOfMonthError != null) Text(dayOfMonthError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Mes del Año (solo si es Anual)
            if (selectedFrequency == RecurrenceFrequency.YEARLY) {
                OutlinedTextField(
                    value = monthOfYearString,
                    onValueChange = { monthOfYearString = it.filter { char -> char.isDigit() }; monthOfYearError = null },
                    label = { Text("Mes del Año (1-12, opcional)") },
                    placeholder = { Text("Ej: 3 para Marzo")},
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = monthOfYearError != null,
                    singleLine = true
                )
                if (monthOfYearError != null) Text(monthOfYearError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
            }


            // Fecha de Finalización
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = hasEndDate, onCheckedChange = {
                    hasEndDate = it
                    if (!it) endDateMillis = null // Limpiar fecha si se desmarca
                })
                Text("Establecer fecha de finalización")
            }
            if (hasEndDate) {
                DateField("Fecha de Fin", endDateMillis, dateFormatter) { showEndDatePicker = true }
            }
            Spacer(modifier = Modifier.height(12.dp))


            // Activa / Inactiva
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { isActive = !isActive }) {
                Switch(checked = isActive, onCheckedChange = { isActive = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isActive) "Plantilla Activa" else "Plantilla Inactiva")
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        var isValid = true
                        // Validaciones
                        if (name.isBlank()) { nameError = "Nombre obligatorio"; isValid = false }
                        val amountDouble = amountString.toDoubleOrNull()
                        if (amountDouble == null || amountDouble <= 0) { amountError = "Monto inválido"; isValid = false }
                        if (selectedCategory == null) { categoryError = "Categoría obligatoria"; isValid = false }

                        val intervalInt = intervalString.toIntOrNull()
                        if (intervalInt == null || intervalInt <= 0) { intervalError = "Intervalo inválido"; isValid = false }

                        var dayOfMonthInt: Int? = null
                        if (dayOfMonthString.isNotBlank()) {
                            dayOfMonthInt = dayOfMonthString.toIntOrNull()
                            if (dayOfMonthInt == null || dayOfMonthInt < 1 || dayOfMonthInt > 31) {
                                dayOfMonthError = "Día inválido (1-31)"; isValid = false
                            }
                        }
                        var monthOfYearInt: Int? = null // 0-11 para Calendar
                        if (monthOfYearString.isNotBlank()) {
                            val displayMonth = monthOfYearString.toIntOrNull()
                            if (displayMonth == null || displayMonth < 1 || displayMonth > 12) {
                                monthOfYearError = "Mes inválido (1-12)"; isValid = false
                            } else {
                                monthOfYearInt = displayMonth -1 // Convertir a 0-11
                            }
                        }


                        if (isValid) {
                            onSave(
                                recurringTransactionToEdit?.id,
                                name.trim(),
                                amountDouble!!,
                                description.trim().takeIf { it.isNotBlank() },
                                selectedCategory!!.id,
                                selectedTransactionType,
                                startDateMillis,
                                selectedFrequency,
                                intervalInt!!,
                                dayOfMonthInt,
                                monthOfYearInt,
                                if(hasEndDate) endDateMillis else null,
                                isActive
                            )
                        }
                    }
                ) {
                    Text(if (isEditMode) "Guardar Cambios" else "Crear Plantilla")
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Espacio al final del BottomSheet
        }
    }

    // DatePickers
    if (showStartDatePicker) {
        ShowDatePicker(context, startDateMillis) { millis ->
            startDateMillis = millis
            showStartDatePicker = false
        }
    }
    if (showEndDatePicker) {
        ShowDatePicker(context, endDateMillis ?: System.currentTimeMillis()) { millis ->
            endDateMillis = millis
            showEndDatePicker = false
        }
    }
}

// Composable para mostrar un campo de fecha con un ícono de calendario
@Composable
private fun DateField(
    label: String,
    dateMillis: Long?,
    dateFormatter: SimpleDateFormat,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = dateMillis?.let { dateFormatter.format(it) } ?: "",
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        trailingIcon = { Icon(Icons.Filled.DateRange, "Seleccionar fecha", Modifier.clickable(onClick = onClick)) },
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    )
}

// Composable para mostrar el DatePickerDialog
@Composable
private fun ShowDatePicker(
    context: android.content.Context,
    initialMillis: Long,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialMillis }
    DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val newCal = Calendar.getInstance()
            newCal.set(year, month, dayOfMonth)
            onDateSelected(newCal.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

// Funciones de extensión para mostrar nombres legibles de Enums
fun RecurrenceFrequency.toDisplayString(): String {
    return when (this) {
        RecurrenceFrequency.DAILY -> "Diaria"
        RecurrenceFrequency.WEEKLY -> "Semanal"
        RecurrenceFrequency.MONTHLY -> "Mensual"
        RecurrenceFrequency.YEARLY -> "Anual"
    }
}
fun RecurrenceFrequency.toUnitString(interval: Int): String {
    val plural = interval > 1
    return when (this) {
        RecurrenceFrequency.DAILY -> if (plural) "días" else "día"
        RecurrenceFrequency.WEEKLY -> if (plural) "semanas" else "semana"
        RecurrenceFrequency.MONTHLY -> if (plural) "meses" else "mes"
        RecurrenceFrequency.YEARLY -> if (plural) "años" else "año"
    }
}