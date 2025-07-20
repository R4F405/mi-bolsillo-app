package com.rafa.mi_bolsillo_app.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rafa.mi_bolsillo_app.navigation.AppScreens
import com.rafa.mi_bolsillo_app.ui.theme.ExpenseRed
import com.rafa.mi_bolsillo_app.ui.theme.IncomeGreen
import com.rafa.mi_bolsillo_app.ui.transactions.TransactionRowItem
import java.text.NumberFormat
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.HorizontalDivider
import com.rafa.mi_bolsillo_app.ui.budget.BudgetItem
import androidx.compose.material.icons.filled.Settings
import com.rafa.mi_bolsillo_app.ui.theme.LocalIsDarkTheme

/**
 * Pantalla principal del Dashboard.
 * Muestra el balance actual, ingresos, gastos, gráfico de gastos por categoría, movimientos recientes y presupuestos favoritos.
 * Incluye un menú lateral para navegar a otras secciones de la aplicación.
 *
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val numberFormat = remember(uiState.currency) {
        NumberFormat.getCurrencyInstance().apply {
            currency = uiState.currency
        }
    }
    val currentDarkTheme = LocalIsDarkTheme.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    //Hamburgesa de navegación
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Gestión",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                NavigationDrawerItem(
                    label = { Text(text = "Presupuestos") },
                    selected = false,
                    onClick = {
                        navController.navigate(AppScreens.BudgetScreen.route)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.PieChart, contentDescription = "Presupuestos") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(text = "Categorías") },
                    selected = false,
                    onClick = {
                        navController.navigate(AppScreens.CategoryManagementScreen.route)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.List, contentDescription = "Categorías") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(text = "Plantillas Recurrentes") },
                    selected = false,
                    onClick = {
                        navController.navigate(AppScreens.RecurringTransactionListScreen.route)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.Autorenew, contentDescription = "Plantillas Recurrentes") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    "Configuración",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                NavigationDrawerItem(
                    label = { Text(text = "Ajustes") },
                    selected = false,
                    onClick = {
                        navController.navigate(AppScreens.SettingsScreen.route)
                        scope.launch { drawerState.close() }
                              },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Ajustes") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        // Contenido principal del Dashboard
        Scaffold(
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

                TopAppBar(
                    title = { Text("Mi Bolsillo") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, "Menú")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = topAppBarContainerColor,
                        titleContentColor = topAppBarContentColor,
                        navigationIconContentColor = topAppBarContentColor
                    )
                )
            },
            // Botón flotante para añadir transacciones
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(AppScreens.AddTransactionScreen.createRoute(null))
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir transacción")
                }
            }
        // Selector de mes
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
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
                // Muestra el balance actual, ingresos y gastos
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "Balance Actual",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = numberFormat.format(uiState.balance),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                        color = if (uiState.balance >= 0) MaterialTheme.colorScheme.onBackground else ExpenseRed
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Ingresos", style = MaterialTheme.typography.labelLarge)
                            Text(
                                numberFormat.format(uiState.totalIncome),
                                style = MaterialTheme.typography.titleLarge,
                                color = IncomeGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Gastos", style = MaterialTheme.typography.labelLarge)
                            Text(
                                numberFormat.format(uiState.totalExpenses),
                                style = MaterialTheme.typography.titleLarge,
                                color = ExpenseRed
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(35.dp))

                    // Gráfico de gastos por categoría
                    Text("Gráfico de Gastos", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(15.dp))

                    if (uiState.expensesByCategory.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            CategoryPieChart(
                                expensesByCategory = uiState.expensesByCategory,
                                currency = uiState.currency,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay datos de gastos para mostrar en el gráfico.")
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    // Movimientos recientes
                    Text("Movimientos Recientes", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.recentTransactions.isEmpty()) {
                        Text(
                            "No hay movimientos recientes este mes.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Column {
                            uiState.recentTransactions.forEach { transactionItem ->
                                TransactionRowItem(
                                    transactionItem = transactionItem,
                                    currency = uiState.currency,
                                    onItemClick = {
                                        navController.navigate(AppScreens.AddTransactionScreen.createRoute(transactionItem.id))
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón para ver historial completo de transacciones
                    OutlinedButton(
                        onClick = { navController.navigate(AppScreens.TransactionHistoryScreen.route) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Ver Historial Completo")
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Presupuestos favoritos
                    if (uiState.favoriteBudgets.isNotEmpty()) {
                        Text("Presupuestos Favoritos", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            uiState.favoriteBudgets.forEach { budgetItem ->
                                Box(
                                    modifier = Modifier.clickable {
                                        // Al pulsar, navegamos a la pantalla de presupuestos
                                        navController.navigate(AppScreens.BudgetScreen.route)
                                    }
                                ) {
                                    BudgetItem(
                                        item = budgetItem,
                                        currency = uiState.currency,
                                        showActions = false, // Ocultamos los botones de edición y borrado
                                        onToggleFavorite = {},
                                        onEditClick = {},
                                        onDeleteClick = {}
                                    )
                                }
                            }
                        }
                    }
                    // Dejar espacio para evitar el solapamiento del FAB
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}