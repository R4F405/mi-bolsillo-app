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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES")) // Formato para España (€)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Bolsillo") }, // O el nombre de tu app
                actions = {
                    // Placeholder para selector de Mes/Año
                    // IconButton(onClick = { /* viewModel.selectPreviousMonth() */ }) {
                    //     Icon(Icons.Filled.ArrowBack, contentDescription = "Mes anterior")
                    // }
                    Text(uiState.monthName, modifier = Modifier.padding(horizontal = 8.dp))
                    // IconButton(onClick = { /* viewModel.selectNextMonth() */ }) {
                    //     Icon(Icons.Filled.ArrowForward, contentDescription = "Mes siguiente")
                    // }
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
                // Navegamos a la ruta base, transactionId tomará el valor por defecto (-1L)
                navController.navigate(AppScreens.AddTransactionScreen.createRoute(null))
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir transacción")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp) // Padding general para el contenido
                .fillMaxSize()
            // Considera .verticalScroll(rememberScrollState()) si el contenido siempre es más alto que la pantalla
        ) {
            // Sección de Balance
            Text("Balance Actual", style = MaterialTheme.typography.titleMedium)
            Text(
                text = numberFormat.format(uiState.balance),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp), // Un poco más grande
                color = if (uiState.balance >= 0) MaterialTheme.colorScheme.onBackground else AppExpense
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Sección Ingresos y Gastos
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
            Spacer(modifier = Modifier.height(24.dp))

            // Sección Gráfico (Placeholder)
            Text("Gráfico de Gastos", style = MaterialTheme.typography.titleMedium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Altura para el gráfico
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Aquí irá el Pie Chart (MPAndroidChart próximamente)")
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Sección Movimientos Recientes
            Text("Movimientos Recientes", style = MaterialTheme.typography.titleMedium)
            if (uiState.recentTransactions.isEmpty()) {
                Text(
                    "No hay movimientos recientes este mes.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    uiState.recentTransactions.forEach { transactionItem ->
                        TransactionRowItem(
                            transactionItem = transactionItem,
                            onItemClick = { // <-- AÑADE ESTA LAMBDA
                                // Navegar a la pantalla de edición pasando el ID
                                navController.navigate("${AppScreens.AddTransactionScreen.route}?transactionId=${transactionItem.id}")
                            }
                        )
                        // Divider() // Opcional
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f)) // Empuja el botón hacia abajo

            // Botón Ver Historial Completo
            OutlinedButton(
                onClick = { navController.navigate(AppScreens.TransactionHistoryScreen.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver Historial Completo")
            }
        }
    }
}