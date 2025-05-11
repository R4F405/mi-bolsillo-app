package com.rafa.mi_bolsillo_app.ui.add_transaction

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Para el icono de "atrás"
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
import com.rafa.mi_bolsillo_app.ui.transactions.TransactionViewModel // Reutilizaremos TransactionViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class) // Si usas componentes experimentales de M3
@Composable
fun AddTransactionScreen(
    navController: NavController,
    transactionId: Long,
    viewModel: TransactionViewModel = hiltViewModel() // O un AddTransactionViewModel dedicado
) {
    var concepto by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    var selectedDateMillis by rememberSaveable { mutableStateOf(calendar.timeInMillis) }
    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var selectedTransactionType by remember { mutableStateOf(TransactionType.EXPENSE) }

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryError by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Transacción") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Para volver atrás
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
                .verticalScroll(rememberScrollState()), // Para que el formulario sea scrollable si es largo
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Selector de Tipo (Ingreso/Gasto)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TransactionType.values().forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp).clickable { selectedTransactionType = type }
                    ) {
                        RadioButton(selected = (type == selectedTransactionType), onClick = { selectedTransactionType = type })
                        Text(text = if (type == TransactionType.INCOME) "Ingreso" else "Gasto", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Campo de Concepto
            OutlinedTextField(value = concepto, onValueChange = { concepto = it }, label = { Text("Concepto (Opcional)") }, singleLine = false, modifier = Modifier.fillMaxWidth(), maxLines = 3)
            Spacer(modifier = Modifier.height(12.dp))

            // Campo de Monto
            OutlinedTextField(value = amount, onValueChange = { amount = it; amountError = null }, label = { Text("Monto") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), isError = amountError != null, supportingText = { if (amountError != null) Text(amountError!!) })
            Spacer(modifier = Modifier.height(12.dp))

            // Selector de Fecha
            OutlinedTextField(
                value = dateFormatter.format(selectedDateMillis),
                onValueChange = {},
                label = { Text("Fecha") },
                readOnly = true,
                trailingIcon = { Icon(Icons.Filled.DateRange, "Seleccionar fecha") },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Selector de Categoría
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "Seleccionar categoría",
                    onValueChange = { /* No editable */ },
                    label = { Text("Categoría") }, readOnly = true,
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "Abrir categorías") },
                    modifier = Modifier.fillMaxWidth().clickable {
                        if (categories.isNotEmpty()) categoryMenuExpanded = true else categoryError = "No hay categorías"
                    },
                    isError = categoryError != null, supportingText = { if (categoryError != null) Text(categoryError!!) }
                )
                DropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
                    if (categories.isEmpty()) {
                        DropdownMenuItem(text = { Text("No hay categorías") }, onClick = { categoryMenuExpanded = false }, enabled = false)
                    } else {
                        categories.forEach { category ->
                            DropdownMenuItem(text = { Text(category.name) }, onClick = {
                                selectedCategory = category; categoryMenuExpanded = false; categoryError = null
                            })
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Botón de Guardar
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    var isValid = true
                    amountError = null; categoryError = null

                    if (amountDouble == null || amountDouble <= 0) { amountError = "Monto inválido"; isValid = false }
                    if (selectedCategory == null) { categoryError = "Selecciona una categoría"; isValid = false }

                    if (isValid && amountDouble != null && selectedCategory != null) {
                        viewModel.addTransaction(
                            amount = amountDouble,
                            date = selectedDateMillis, // Usamos la fecha seleccionada
                            concepto = concepto.takeIf { it.isNotBlank() },
                            categoryId = selectedCategory!!.id,
                            type = selectedTransactionType
                        )
                        navController.popBackStack() // Volver a la pantalla anterior (Dashboard)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) { Text("Confirmar Transacción", style = MaterialTheme.typography.labelLarge) }
        }
    }

    // DatePickerDialog
    if (showDatePicker) {
        val context = LocalContext.current
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                selectedDateMillis = cal.timeInMillis
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
        // Para que el dialog no se quede en el estado 'true' si el usuario lo cancela externamente
        LaunchedEffect(Unit) {
            // Este es un truco, ya que el DatePickerDialog no tiene un onDismissRequest directo en Compose que podamos usar fácilmente aquí.
            // Si el usuario cancela, showDatePicker seguirá siendo true.
            // Una solución más robusta podría implicar un wrapper para el Dialog.
            // Por ahora, lo dejamos así, el usuario debe seleccionar una fecha o volver atrás.
            // O, si no se selecciona fecha, no actualizamos 'showDatePicker = false' dentro del listener del DatePickerDialog
            // y esperamos que el usuario lo cancele, o ponemos un botón de cancelar explícito.
            // Para simplificar: al seleccionar fecha, se cierra. Si cancela, debe volver atrás.
        }
    }
}

// Preview (necesitaría un NavController de mentira)
@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    MiBolsilloAppTheme {
         // Necesitarías un NavController de prueba y un transactionId de ejemplo
         AddTransactionScreen(navController = rememberNavController(), transactionId = -1L)
    }
}