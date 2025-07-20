package com.rafa.mi_bolsillo_app.ui.budget

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rafa.mi_bolsillo_app.ui.category_management.CategoryColorIndicator
import com.rafa.mi_bolsillo_app.ui.components.ConfirmationDialog
import com.rafa.mi_bolsillo_app.ui.theme.LocalIsDarkTheme
import java.text.NumberFormat
import java.util.Currency

/**
 * Pantalla principal de Presupuestos.
 * Muestra una lista de presupuestos para el mes actual y permite añadir, editar o eliminar presupuestos.
 *
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    navController: NavController,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentDarkTheme = LocalIsDarkTheme.current

    var showAddEditDialog by remember { mutableStateOf(false) }
    var budgetToEdit by remember { mutableStateOf<BudgetUiItem?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var budgetToDelete by remember { mutableStateOf<BudgetUiItem?>(null) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val topAppBarContainerColor = if (currentDarkTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
            val topAppBarContentColor = if (currentDarkTheme) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary

            TopAppBar(
                title = { Text("Presupuestos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topAppBarContainerColor,
                    titleContentColor = topAppBarContentColor,
                    navigationIconContentColor = topAppBarContentColor,
                )
            )
        },
        // Botón flotante para añadir un nuevo presupuesto
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    budgetToEdit = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, "Añadir presupuesto")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                // Fila para seleccionar el mes actual
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.selectPreviousMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Mes anterior")
                    }
                    Text(
                        text = uiState.monthName,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    IconButton(onClick = { viewModel.selectNextMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Mes siguiente")
                    }
                }
            }

            // Contenedor principal para la lista de presupuestos
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.budgetItems.isEmpty()) {
                    Text(
                        "No hay presupuestos para este mes. ¡Añade uno!",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.budgetItems, key = { it.budget.id }) { item ->
                            BudgetItem(
                                item = item,
                                currency = uiState.currency,
                                onToggleFavorite = { viewModel.toggleFavoriteStatus(item.budget.id) },
                                onEditClick = {
                                    budgetToEdit = it
                                    showAddEditDialog = true
                                },
                                onDeleteClick = {
                                    budgetToDelete = it
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }

                // Diálogo de confirmación para eliminar presupuesto
                if (showDeleteDialog) {
                    ConfirmationDialog(
                        showDialog = true,
                        onConfirm = {
                            budgetToDelete?.let { viewModel.deleteBudget(it.budget.id) }
                            showDeleteDialog = false
                        },
                        onDismiss = { showDeleteDialog = false },
                        title = "Eliminar Presupuesto",
                        message = "¿Seguro que quieres eliminar el presupuesto para la categoría '${budgetToDelete?.category?.name}'?",
                        icon = Icons.Filled.Warning
                    )
                }
            }

            // Diálogo para añadir/editar presupuesto
            if (showAddEditDialog) {
                AddEditBudgetDialog(
                    budgetToEdit = budgetToEdit,
                    availableCategories = uiState.availableCategories,
                    onDismiss = { showAddEditDialog = false },
                    onConfirm = { categoryId, amount ->
                        viewModel.upsertBudget(categoryId, amount)
                        showAddEditDialog = false
                    }
                )
            }
        }
    }
}

/**
 * Componente que representa un ítem de presupuesto en la lista.
 * Muestra la categoría, el monto gastado, el total del presupuesto y un indicador de progreso.
 * Permite marcar como favorito, editar o eliminar el presupuesto.
 *
 */
@Composable
fun BudgetItem(
    item: BudgetUiItem,
    currency: Currency,
    onToggleFavorite: () -> Unit,
    onEditClick: (BudgetUiItem) -> Unit,
    onDeleteClick: (BudgetUiItem) -> Unit,
    showActions: Boolean = true
) {
    // Formato de moneda
    val currencyFormat = remember(currency) {
        NumberFormat.getCurrencyInstance().apply {
            this.currency = currency
        }
    }

    // Cálculo del progreso y color del progreso
    val progress = if (item.budget.amount > 0) (item.spentAmount / item.budget.amount).toFloat() else 0f
    val progressColor = when {
        progress > 1.0f -> MaterialTheme.colorScheme.error
        progress > 0.85f -> Color(0xFFFFA000)
        else -> MaterialTheme.colorScheme.primary
    }

    // Diseño de la tarjeta del ítem de presupuesto
    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .animateContentSize()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showActions) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (item.budget.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "Marcar como favorito",
                            tint = if (item.budget.isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                CategoryColorIndicator(hexColor = item.category.colorHex)
                Spacer(Modifier.width(8.dp))
                Text(item.category.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (showActions) {
                    Row {
                        IconButton(
                            onClick = { onEditClick(item) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                "Editar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { onDeleteClick(item) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Gastado: ${currencyFormat.format(item.spentAmount)}", fontSize = 14.sp)
                Text("Total: ${currencyFormat.format(item.budget.amount)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }
    }
}