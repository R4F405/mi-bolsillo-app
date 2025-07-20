package com.rafa.mi_bolsillo_app.ui.add_transaction

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.ui.theme.MiBolsilloAppTheme
import com.rafa.mi_bolsillo_app.ui.transactions.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.material.icons.filled.Delete
import com.rafa.mi_bolsillo_app.ui.components.ConfirmationDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Warning
import com.rafa.mi_bolsillo_app.ui.theme.LocalIsDarkTheme

/**
 * Composable para la pantalla de añadir o editar una transacción.
 *
 * Permite al usuario añadir o editar una transacción existente.
 *
 */

@OptIn(ExperimentalMaterial3Api::class) // Necesario para TopAppBar, Scaffold y ExposedDropdownMenuBox
@Composable
fun AddTransactionScreen(
    navController: NavController,
    transactionId: Long, // -1L para nueva transacción
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val isEditMode = transactionId != -1L
    val currentDarkTheme = LocalIsDarkTheme.current

    var concepto by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }

    val initialCalendar = Calendar.getInstance()
    var selectedDateMillis by rememberSaveable { mutableStateOf(initialCalendar.timeInMillis) }
    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var selectedTransactionType by remember { mutableStateOf(TransactionType.EXPENSE) }

    val categoriesFromVm by viewModel.categories.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryError by rememberSaveable { mutableStateOf<String?>(null) }

    val transactionToEdit by viewModel.transactionToEdit.collectAsStateWithLifecycle()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Cargar la transacción si estamos en modo edición
    LaunchedEffect(key1 = transactionId) {
        if (isEditMode) {
            viewModel.loadTransactionForEditing(transactionId)
        } else {
            viewModel.clearEditingTransaction() // Asegura limpiar el estado al entrar en modo "nuevo"
            concepto = ""
            amount = ""
            selectedDateMillis = System.currentTimeMillis()
            selectedTransactionType = TransactionType.EXPENSE
            selectedCategory = null // Asegura que no haya categoría preseleccionada
            amountError = null
            categoryError = null
        }
    }

    // Actualizar la transacción si es necesario
    LaunchedEffect(key1 = transactionToEdit, key2 = categoriesFromVm, key3 = isEditMode) {
        if (isEditMode && transactionToEdit != null) {
            val tx = transactionToEdit!!
            concepto = tx.description ?: ""
            amount = tx.amount.toString()
            selectedDateMillis = tx.date
            selectedTransactionType = tx.transactionType
            // Asegura que la categoría exista en la lista actual antes de asignarla
            selectedCategory = categoriesFromVm.find { it.id == tx.categoryId }
        }
    }

    // Limpiar la transacción de edición cuando el Composable se va
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearEditingTransaction()
        }
    }

    Scaffold(
        topBar = {
            // Determinar colores de la TopAppBar basados en el tema actual
            val topAppBarContainerColor = if (currentDarkTheme) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primary
            }
            val topAppBarContentColor = if (currentDarkTheme) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onPrimary
            }

            // Configurar la TopAppBar con el título correspondiente
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Transacción" else "Nueva Transacción") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topAppBarContainerColor,
                    titleContentColor = topAppBarContentColor,
                    navigationIconContentColor = topAppBarContentColor
                )
            )
        }
        // Contenido principal de la pantalla
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Campos de entrada para concepto
            OutlinedTextField(
                value = concepto,
                onValueChange = { concepto = it },
                label = { Text("Concepto (Opcional)") },
                singleLine = false, // Permite múltiples líneas
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            // Campo de entrada para monto
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it; amountError = null },
                label = { Text("Monto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = amountError != null,
                supportingText = { if (amountError != null) Text(amountError!!) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Campo de entrada para fecha
            Text(
                text = "Fecha",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { showDatePicker = true })
            ) {
                OutlinedTextField(
                    value = dateFormatter.format(selectedDateMillis),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = "Seleccionar fecha") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Campo para categoría
            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = {
                    // Solo expandir si hay categorías, o si se quiere mostrar un mensaje dentro
                    if (categoriesFromVm.isNotEmpty()) { // Evitar toggle si no hay categorías y ya se mostró error
                        categoryMenuExpanded = !categoryMenuExpanded
                    } else {
                        categoryError = "No hay categorías disponibles. Añade alguna primero."
                        categoryMenuExpanded = false // No expandir si no hay nada que mostrar
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "",
                    onValueChange = { /* No editable por texto */ },
                    label = { Text("Categoría") },
                    placeholder = { Text(if (categoriesFromVm.isEmpty()) "No hay categorías" else "Seleccionar categoría") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    isError = categoryError != null,
                    supportingText = { if (categoryError != null) Text(categoryError!!) }
                )
                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded && categoriesFromVm.isNotEmpty(), // Solo mostrar si está expandido Y hay categorías
                    onDismissRequest = { categoryMenuExpanded = false },
                ) {
                    categoriesFromVm.forEach { category ->
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
            Spacer(modifier = Modifier.height(32.dp))

            // Botón para confirmar o guardar cambios
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    var isValid = true
                    amountError = null; categoryError = null // Resetear errores

                    if (amountDouble == null || amountDouble <= 0) {
                        amountError = "Monto inválido"
                        isValid = false
                    }
                    if (selectedCategory == null) {
                        categoryError = "Selecciona una categoría"
                        isValid = false
                    }

                    if (isValid && amountDouble != null && selectedCategory != null) {
                        if (isEditMode && transactionId != -1L) { // Doble chequeo para editar
                            viewModel.updateTransaction(
                                transactionId = transactionId,
                                amount = amountDouble,
                                date = selectedDateMillis,
                                concepto = concepto.trim().takeIf { it.isNotBlank() },
                                categoryId = selectedCategory!!.id,
                                type = selectedTransactionType
                            )
                        } else {
                            viewModel.addTransaction(
                                amount = amountDouble,
                                date = selectedDateMillis,
                                concepto = concepto.trim().takeIf { it.isNotBlank() },
                                categoryId = selectedCategory!!.id,
                                type = selectedTransactionType
                            )
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(
                    if (isEditMode) "Guardar Cambios" else "Confirmar Transacción",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            //Boton eliminar solo si estamos en modo edición
            if (isEditMode) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    )
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Eliminar transacción",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Eliminar Transacción", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    // Dialogo de confirmación de eliminación
    ConfirmationDialog(
        showDialog = showDeleteConfirmDialog,
        onConfirm = {
            if (transactionId != -1L) {
                viewModel.deleteTransaction(transactionId)
            }
            showDeleteConfirmDialog = false
            navController.popBackStack()
        },
        onDismiss = { showDeleteConfirmDialog = false },
        title = "Confirmar Eliminación",
        message = "La transacción será eliminada permanentemente.\n¿Estás seguro de que quieres continuar?",
        confirmButtonText = "Eliminar",
        icon = Icons.Filled.Warning
    )

    // Mostrar DatePicker si showDatePicker es true
    if (showDatePicker) {
        val context = LocalContext.current
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val newCal = Calendar.getInstance()
                newCal.set(year, month, dayOfMonth)
                selectedDateMillis = newCal.timeInMillis
                showDatePicker = false // Ocultar DatePickerDialog después de seleccionar
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { showDatePicker = false } // Asegurar que se oculte si se descarta
            show()
        }
    }
}

// Vista previa de la pantalla AddTransactionScreen
@Preview(showBackground = true, name = "Add Transaction Light")
@Composable
fun AddTransactionScreenNewPreviewLight() {
    MiBolsilloAppTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            // Simular NavController y ViewModel para la preview
            val navController = NavController(LocalContext.current)
            // Aquí necesitarías un mock/fake ViewModel si la preview lo requiere para estado inicial
            AddTransactionScreen(navController = navController, transactionId = -1L)
        }
    }
}

// Vista previa de la pantalla AddTransactionScreen en modo oscuro
@Preview(showBackground = true, name = "Add Transaction Dark")
@Composable
fun AddTransactionScreenNewPreviewDark() {
    MiBolsilloAppTheme{
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val navController = NavController(LocalContext.current)
            AddTransactionScreen(navController = navController, transactionId = -1L)
        }
    }
}