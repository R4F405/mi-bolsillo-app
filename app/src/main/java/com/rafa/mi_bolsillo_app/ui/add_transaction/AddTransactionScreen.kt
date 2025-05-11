package com.rafa.mi_bolsillo_app.ui.add_transaction

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    transactionId: Long, // Recibido desde la navegación, -1L para nueva transacción
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val isEditMode = transactionId != -1L

    // Estados para los campos del formulario (manejados localmente en el Composable)
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

    // Observar la transacción que se está editando desde el ViewModel
    val transactionToEdit by viewModel.transactionToEdit.collectAsStateWithLifecycle()

    // Efecto para cargar la transacción si estamos en modo edición,
    // o para limpiar el estado si es una nueva transacción.
    LaunchedEffect(key1 = transactionId) {
        if (isEditMode) {
            viewModel.loadTransactionForEditing(transactionId)
        } else {
            viewModel.clearEditingTransaction() // Limpia el estado en el VM
            // Resetea los campos del formulario para "Nueva Transacción"
            concepto = ""
            amount = ""
            selectedDateMillis = System.currentTimeMillis()
            selectedTransactionType = TransactionType.EXPENSE
            selectedCategory = null
            amountError = null
            categoryError = null
        }
    }

    // Efecto para rellenar el formulario cuando transactionToEdit cambia (en modo edición)
    // y cuando las categorías están disponibles (para encontrar el objeto Category)
    LaunchedEffect(key1 = transactionToEdit, key2 = categoriesFromVm, key3 = isEditMode) {
        if (isEditMode && transactionToEdit != null) {
            val tx = transactionToEdit!!
            concepto = tx.description ?: "" // La entidad usa 'description'
            amount = tx.amount.toString()
            selectedDateMillis = tx.date
            selectedTransactionType = tx.transactionType
            // Busca el objeto Category completo basado en el categoryId de la transacción
            selectedCategory = categoriesFromVm.find { it.id == tx.categoryId }
        }
    }

    // Efecto para limpiar el estado del ViewModel cuando el Composable se va
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearEditingTransaction()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Transacción" else "Nueva Transacción") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Selector de Tipo (Ingreso/Gasto)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center) {
                TransactionType.values().forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp).clickable { selectedTransactionType = type }) {
                        RadioButton(selected = (type == selectedTransactionType), onClick = { selectedTransactionType = type })
                        Text(text = if (type == TransactionType.INCOME) "Ingreso" else "Gasto", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            OutlinedTextField(value = concepto, onValueChange = { concepto = it }, label = { Text("Concepto (Opcional)") }, singleLine = false, modifier = Modifier.fillMaxWidth(), maxLines = 3)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it; amountError = null }, label = { Text("Monto") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), isError = amountError != null, supportingText = { if (amountError != null) Text(amountError!!) })
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = dateFormatter.format(selectedDateMillis), onValueChange = {}, label = { Text("Fecha") }, readOnly = true, trailingIcon = { Icon(Icons.Filled.DateRange, "Seleccionar fecha") }, modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true })
            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "Seleccionar categoría",
                    onValueChange = { /* No editable */ }, label = { Text("Categoría") }, readOnly = true,
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "Abrir categorías") },
                    modifier = Modifier.fillMaxWidth().clickable {
                        if (categoriesFromVm.isNotEmpty()) categoryMenuExpanded = true else categoryError = "No hay categorías"
                    },
                    isError = categoryError != null, supportingText = { if (categoryError != null) Text(categoryError!!) }
                )
                DropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
                    if (categoriesFromVm.isEmpty()) {
                        DropdownMenuItem(text = { Text("No hay categorías") }, onClick = { categoryMenuExpanded = false }, enabled = false)
                    } else {
                        categoriesFromVm.forEach { category -> DropdownMenuItem(text = { Text(category.name) }, onClick = { selectedCategory = category; categoryMenuExpanded = false; categoryError = null }) }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    var isValid = true
                    amountError = null; categoryError = null

                    if (amountDouble == null || amountDouble <= 0) { amountError = "Monto inválido"; isValid = false }
                    if (selectedCategory == null) { categoryError = "Selecciona una categoría"; isValid = false }

                    if (isValid && amountDouble != null && selectedCategory != null) {
                        if (isEditMode) {
                            viewModel.updateTransaction(
                                transactionId = transactionId, // El ID original de la transacción que se está editando
                                amount = amountDouble,
                                date = selectedDateMillis,
                                concepto = concepto.takeIf { it.isNotBlank() },
                                categoryId = selectedCategory!!.id,
                                type = selectedTransactionType
                            )
                        } else {
                            viewModel.addTransaction(
                                amount = amountDouble,
                                date = selectedDateMillis,
                                concepto = concepto.takeIf { it.isNotBlank() },
                                categoryId = selectedCategory!!.id,
                                type = selectedTransactionType
                            )
                        }
                        navController.popBackStack() // Volver a la pantalla anterior
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) { Text(if (isEditMode) "Guardar Cambios" else "Confirmar Transacción", style = MaterialTheme.typography.labelLarge) }
        }
    }

    if (showDatePicker) {
        val context = LocalContext.current
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val newCal = Calendar.getInstance()
                newCal.set(year, month, dayOfMonth)
                selectedDateMillis = newCal.timeInMillis
                showDatePicker = false
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Opcional: Limitar fechas si es necesario (ej. setMaxDate)
            setOnDismissListener { showDatePicker = false } // Asegura que showDatePicker se ponga a false si se cancela
            show()
        }
    }
}

// Preview (simplificado, necesitaría mocks para ViewModel y NavController para ser más útil)
@Preview(showBackground = true)
@Composable
fun AddTransactionScreenNewPreview() {
    MiBolsilloAppTheme {
        Surface {
            // Para un preview de "nueva transacción", transactionId sería -1L
            // Se necesitaría un NavController y ViewModel mockeados para un preview completo.
            Text("Preview de AddTransactionScreen (Modo Nueva)")
        }
    }
}