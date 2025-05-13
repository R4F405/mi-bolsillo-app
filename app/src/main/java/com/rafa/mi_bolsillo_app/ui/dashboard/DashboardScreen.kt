package com.rafa.mi_bolsillo_app.ui.dashboard

import androidx.compose.foundation.isSystemInDarkTheme // ¡IMPORTANTE AÑADIR ESTE IMPORT!
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // Para el scroll
import androidx.compose.foundation.verticalScroll // Para el scroll
// imports no usados de lazy column eliminados para limpieza si no se usan en este archivo específico
// import androidx.compose.foundation.lazy.LazyColumn
// import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
// import androidx.compose.material.icons.filled.ArrowBack // Reemplazado por automirrored
// import androidx.compose.material.icons.filled.ArrowForward // Reemplazado por automirrored
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
import com.rafa.mi_bolsillo_app.ui.model.TransactionUiItem // Asumo que es para TransactionRowItem
import com.rafa.mi_bolsillo_app.ui.theme.AppExpense // Asumiendo que existen en Color.kt
import com.rafa.mi_bolsillo_app.ui.theme.AppIncome // Asumiendo que existen en Color.kt
import com.rafa.mi_bolsillo_app.ui.transactions.TransactionRowItem // Reutilizamos el Composable
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material.icons.filled.Settings // O cualquier otro icono que te guste


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    val currentDarkTheme = isSystemInDarkTheme() // Detecta si el tema oscuro del sistema está activo

    Scaffold(
        topBar = {
            // Determinar colores de la TopAppBar basados en el tema actual
            val topAppBarContainerColor = if (currentDarkTheme) {
                MaterialTheme.colorScheme.surface // Usar color de superficie para modo oscuro
            } else {
                MaterialTheme.colorScheme.primary // Usar color primario para modo claro
            }
            val topAppBarContentColor = if (currentDarkTheme) {
                MaterialTheme.colorScheme.onSurface // Contenido sobre superficie para modo oscuro
            } else {
                MaterialTheme.colorScheme.onPrimary // Contenido sobre primario para modo claro
            }

            TopAppBar(
                title = { Text("Mi Bolsillo") },
                actions = {
                    // Selector de Mes/Año
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

                    // --- BOTÓN PARA IR A GESTIÓN DE CATEGORÍAS ---
                    IconButton(onClick = {
                        navController.navigate(AppScreens.CategoryManagementScreen.route)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Settings, // O usa Icons.Filled.Category o el que prefieras
                            contentDescription = "Gestionar Categorías"
                        )
                    }
                    // --- FIN BOTÓN ---
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topAppBarContainerColor,
                    titleContentColor = topAppBarContentColor,
                    actionIconContentColor = topAppBarContentColor // Aplicar también a los iconos de acción
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(AppScreens.AddTransactionScreen.createRoute(null))
                },
                // Los colores del FAB se mantienen para que sea un acento en ambos temas
                // (azul oscuro en tema claro, azul claro en tema oscuro)
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
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
                    .padding(top = 16.dp, bottom = 16.dp) // Añadido padding inferior también
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
                    color = if (uiState.balance >= 0) MaterialTheme.colorScheme.onBackground else AppExpense // Considera usar MaterialTheme.colorScheme.error para gastos
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
                            color = AppIncome // Considera usar un color del theme si es posible/deseado
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Gastos", style = MaterialTheme.typography.labelLarge)
                        Text(
                            numberFormat.format(uiState.totalExpenses),
                            style = MaterialTheme.typography.titleLarge,
                            color = AppExpense // Considera usar MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(modifier = Modifier.height(35.dp))

                // Gráfico de Gastos
                Text("Gráfico de Gastos", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(15.dp)) //Bajar el grafico

                if (uiState.expensesByCategory.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(vertical = 4.dp) // Un poco de padding vertical para el gráfico
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
                            .height(160.dp) // Misma altura para consistencia
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay datos de gastos para mostrar en el gráfico.")
                    }
                }

                Spacer(modifier = Modifier.height(15.dp)) // Reducido para dar más espacio a la lista

                // Movimientos Recientes
                Text("Movimientos Recientes", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.recentTransactions.isEmpty()) {
                    Text(
                        "No hay movimientos recientes este mes.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 80.dp) // Asegurar espacio si la lista está vacía
                    )
                } else {
                    // Usar Column en lugar de forEach directamente para mejor manejo del layout dentro de un Column scrolleable
                    Column {
                        uiState.recentTransactions.forEach { transactionItem ->
                            TransactionRowItem(
                                transactionItem = transactionItem,
                                onItemClick = {
                                    navController.navigate(AppScreens.AddTransactionScreen.createRoute(transactionItem.id))
                                }
                            )
                            // Divider(modifier = Modifier.padding(horizontal = 16.dp)) // Opcional: un divisor entre items
                        }
                    }
                    // Espacio para el botón fijo de abajo
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // Botón fijo en la parte inferior
            OutlinedButton(
                onClick = { navController.navigate(AppScreens.TransactionHistoryScreen.route) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp) // Padding para separarlo de los bordes
            ) {
                Text("Ver Historial Completo")
            }
        }
    }
}