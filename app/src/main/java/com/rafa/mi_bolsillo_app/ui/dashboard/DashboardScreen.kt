package com.rafa.mi_bolsillo_app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rafa.mi_bolsillo_app.navigation.AppScreens
import com.rafa.mi_bolsillo_app.ui.theme.AppIncome // Asumiendo que existen en Color.kt
import com.rafa.mi_bolsillo_app.ui.theme.AppExpense
import com.rafa.mi_bolsillo_app.ui.transactions.TransactionRowItem // Reutilizamos el Composable
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // Para el scroll
import androidx.compose.foundation.verticalScroll // Para el scroll
import com.rafa.mi_bolsillo_app.ui.model.TransactionUiItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Bolsillo") },
                actions = {
                    Text(uiState.monthName, modifier = Modifier.padding(horizontal = 16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(AppScreens.AddTransactionScreen.createRoute(null))
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir transacción")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Contenido principal con scroll
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Balance Actual
                Text(
                    text = "Balance Actual",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = numberFormat.format(uiState.balance),
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                    color = if (uiState.balance >= 0) MaterialTheme.colorScheme.onBackground else AppExpense
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
                            color = AppIncome
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Gastos", style = MaterialTheme.typography.labelLarge)
                        Text(
                            numberFormat.format(uiState.totalExpenses),
                            style = MaterialTheme.typography.titleLarge,
                            color = AppExpense
                        )
                    }
                }
                Spacer(modifier = Modifier.height(35.dp))

                // Gráfico de Gastos
                Text("Gráfico de Gastos", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(50.dp)) //Bajar el grafico
                
                if (uiState.expensesByCategory.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
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
                            .height(120.dp)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay datos de gastos para mostrar en el gráfico.")
                    }
                }
                
                Spacer(modifier = Modifier.height(50.dp))

                // Movimientos Recientes
                Text("Movimientos Recientes", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                if (uiState.recentTransactions.isEmpty()) {
                    Text(
                        "No hay movimientos recientes este mes.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    uiState.recentTransactions.forEach { transactionItem ->
                        TransactionRowItem(
                            transactionItem = transactionItem,
                            onItemClick = {
                                navController.navigate(AppScreens.AddTransactionScreen.createRoute(transactionItem.id))
                            }
                        )
                    }
                }
                
                // Espacio para el botón fijo de abajo
                Spacer(modifier = Modifier.height(80.dp))
            }
            
            // Botón fijo en la parte inferior
            OutlinedButton(
                onClick = { navController.navigate(AppScreens.TransactionHistoryScreen.route) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text("Ver Historial Completo")
            }
        }
    }
}