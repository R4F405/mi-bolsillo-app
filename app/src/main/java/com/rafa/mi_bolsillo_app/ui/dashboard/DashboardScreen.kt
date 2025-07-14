package com.rafa.mi_bolsillo_app.ui.dashboard

import androidx.compose.foundation.isSystemInDarkTheme
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
import com.rafa.mi_bolsillo_app.ui.theme.ExpenseRed // Cambio
import com.rafa.mi_bolsillo_app.ui.theme.IncomeGreen // Cambio
import com.rafa.mi_bolsillo_app.ui.transactions.TransactionRowItem
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    val currentDarkTheme = isSystemInDarkTheme()

    // State para el drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
                    label = { Text(text = "Gestionar Categorías") },
                    selected = false, // Puedes hacer esto dinámico si la ruta actual es la de categorías
                    onClick = {
                        navController.navigate(AppScreens.CategoryManagementScreen.route)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.List, contentDescription = "Categorías") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                // Item para transacciones recurrentes
                NavigationDrawerItem(
                    label = { Text(text = "Plantillas Recurrentes") },
                    selected = false, // Puedes hacerlo dinámico también
                    onClick = {
                        navController.navigate(AppScreens.RecurringTransactionListScreen.route)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.Autorenew, contentDescription = "Plantillas Recurrentes") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    ) {
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
                            Icon(Icons.Filled.Menu, "Menú") // Icono de hamburguesa
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.selectPreviousMonth() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Mes anterior")
                        }
                        Text(
                            text = uiState.monthName,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                        IconButton(onClick = { viewModel.selectNextMonth() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Mes siguiente")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = topAppBarContainerColor,
                        titleContentColor = topAppBarContentColor,
                        actionIconContentColor = topAppBarContentColor,
                        navigationIconContentColor = topAppBarContentColor // Color del icono de hamburguesa
                    )
                )
            },
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
        ) { innerPadding ->
            // La Column principal ahora envuelve ttodo el contenido desplazable
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Aplicar el padding del Scaffold aquí
                    .verticalScroll(rememberScrollState()) // Habilitar scroll para toda la columna
                    .padding(horizontal = 16.dp) // Padding horizontal general
                    .padding(top = 16.dp, bottom = 16.dp) // Padding vertical general (el bottom es para el último elemento)
            ) {
                // Balance Actual
                Text(
                    text = "Balance Actual",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = numberFormat.format(uiState.balance),
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                    color = if (uiState.balance >= 0) MaterialTheme.colorScheme.onBackground else ExpenseRed // Cambio
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Ingresos y Gastos
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

                // Gráfico de Gastos
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

                // Movimientos Recientes
                Text("Movimientos Recientes", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.recentTransactions.isEmpty()) {
                    Text(
                        "No hay movimientos recientes este mes.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column { // Contenedor para las transacciones recientes
                        uiState.recentTransactions.forEach { transactionItem ->
                            TransactionRowItem(
                                transactionItem = transactionItem,
                                onItemClick = {
                                    navController.navigate(AppScreens.AddTransactionScreen.createRoute(transactionItem.id))
                                }
                            )
                        }
                    }
                }

                // Botón "Ver Historial Completo" - movido aquí
                // Se mostrará siempre, debajo de la lista de transacciones o del mensaje de "No hay movimientos"
                Spacer(modifier = Modifier.height(16.dp)) // Espacio antes del botón
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
                Spacer(modifier = Modifier.height(64.dp)) // Espacio para que el FAB no solape el botón
            }
        }
    }
}