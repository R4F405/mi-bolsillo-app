package com.rafa.mi_bolsillo_app.ui.recurring_transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rafa.mi_bolsillo_app.data.local.entity.RecurringTransaction
import com.rafa.mi_bolsillo_app.ui.components.ConfirmationDialog
import com.rafa.mi_bolsillo_app.ui.theme.LocalIsDarkTheme

/**
 * Pantalla de gestión de transacciones recurrentes.
 * Permite ver, añadir, editar y eliminar plantillas de transacciones recurrentes.
 * Esta pantalla muestra una lista de las plantillas existentes y permite al usuario interactuar con ellas.
 *
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionListScreen(
    navController: NavController,
    viewModel: RecurringTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentDarkTheme = LocalIsDarkTheme.current

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var templateToDelete by remember { mutableStateOf<RecurringTransaction?>(null) }

    // Para el BottomSheet de Añadir/Editar
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Para que solo sea expandido o cerrado
    )
    // Efecto para mostrar/ocultar el BottomSheet
    LaunchedEffect(uiState.showEditSheet, sheetState.isVisible) {
        if (uiState.showEditSheet) {
            if (!sheetState.isVisible) { // Solo mostrar si no está ya visible
                sheetState.show()
            }
        } else {
            if (sheetState.isVisible) { // Solo ocultar si está visible
                sheetState.hide()
            }
        }
    }
    // Observar cuando el sheet es ocultado por el usuario (arrastrando, etc.)
    LaunchedEffect(sheetState.isVisible) {
        if (!sheetState.isVisible && uiState.showEditSheet) {
            viewModel.dismissEditSheet() // Sincronizar el estado del ViewModel
        }
    }


    // Mostrar mensajes de usuario
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
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
            // Composición de la barra superior
            TopAppBar(
                title = { Text("Plantillas Recurrentes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topAppBarContainerColor,
                    titleContentColor = topAppBarContentColor,
                    navigationIconContentColor = topAppBarContentColor
                )
            )
        },
        // Botón flotante para añadir una nueva plantilla recurrente
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.prepareForEditing(null) }, // null para nueva plantilla
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(Icons.Filled.Add, "Añadir plantilla recurrente")
            }
        }
        // Contenido de la pantalla
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.recurringTransactions.isEmpty()) {
                Text(
                    "No tienes plantillas recurrentes. ¡Crea una!",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.recurringTransactions, key = { it.id }) { template ->
                        val category = uiState.categories.find { it.id == template.categoryId }
                        RecurringTransactionItem(
                            template = template,
                            currency = uiState.currency,
                            categoryName = category?.name,
                            categoryColorHex = category?.colorHex,
                            onEditClick = { viewModel.prepareForEditing(it) },
                            onDeleteClick = {
                                templateToDelete = it
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            }

            // Diálogo de confirmación para eliminar
            ConfirmationDialog(
                showDialog = showDeleteConfirmDialog,
                onConfirm = {
                    templateToDelete?.let { viewModel.deleteRecurringTransaction(it.id) }
                    showDeleteConfirmDialog = false
                    templateToDelete = null
                },
                onDismiss = {
                    showDeleteConfirmDialog = false
                    templateToDelete = null
                },
                title = "Confirmar Eliminación",
                message = "Si eliminas la plantilla '${templateToDelete?.name ?: ""}', no se generarán más transacciones a partir de ella y no podrás recuperarla.\n¿Estás seguro?",
                confirmButtonText = "Eliminar",
                icon = Icons.Filled.Warning
            )

            // ModalBottomSheet para Añadir/Editar
            if (uiState.showEditSheet) {
                AddEditRecurringTransactionSheet(
                    sheetState = sheetState,
                    categories = uiState.categories,
                    recurringTransactionToEdit = uiState.recurringTransactionToEdit,
                    onSave = { id, name, amount, description, categoryId, transactionType, startDate, frequency, interval, dayOfMonth, monthOfYear, endDate, isActive ->
                        viewModel.saveRecurringTransaction(
                            id, name, amount, description, categoryId, transactionType, startDate, frequency, interval, dayOfMonth, monthOfYear, endDate, isActive
                        )
                    },
                    onDismiss = {
                        viewModel.dismissEditSheet()
                    }
                )
            }
        }
    }
}