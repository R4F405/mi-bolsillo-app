package com.rafa.mi_bolsillo_app.ui.transactions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rafa.mi_bolsillo_app.navigation.AppScreens
import com.rafa.mi_bolsillo_app.ui.model.TransactionUiItem
import com.rafa.mi_bolsillo_app.ui.theme.LocalIsDarkTheme
import java.util.Currency

/**
 * Pantalla que muestra el historial de transacciones.
 * Permite buscar transacciones por concepto o categoría.
 * Al hacer clic en una transacción, se navega a la pantalla de edición de transacción.
 *
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    navController: NavController,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentDarkTheme = LocalIsDarkTheme.current

    // Estado para guardar el texto de búsqueda
    var searchQuery by rememberSaveable { mutableStateOf("") }

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

            // Barra superior con título y botón de navegación
            TopAppBar(
                title = { Text("Historial de Transacciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topAppBarContainerColor,
                    titleContentColor = topAppBarContentColor,
                    navigationIconContentColor = topAppBarContentColor
                )
            )
        }
    ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // Barra de Búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por concepto o categoría") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Icono de Búsqueda"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.medium // Esquinas redondeadas
            )

            // Contenedor para la lista o el estado vacío
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.transactions.isEmpty() && searchQuery.isBlank()) {
                    EmptyState(modifier = Modifier.fillMaxSize())
                } else {
                    // Pasamos la lista original y el searchQuery al composable TransactionList
                    TransactionList(
                        transactions = uiState.transactions,
                        currency = uiState.currency, // Pasamos la moneda
                        searchQuery = searchQuery, // Pasar el término de búsqueda
                        onTransactionClick = { transactionId ->
                            navController.navigate(AppScreens.AddTransactionScreen.createRoute(transactionId))
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

// Composable para mostrar la lista de transacciones
@Composable
fun TransactionList(
    transactions: List<TransactionUiItem>,
    currency: Currency,
    searchQuery: String,
    onTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filtramos la lista de transacciones basándonos en el searchQuery
    val filteredTransactions = if (searchQuery.isBlank()) {
        transactions // Si no hay búsqueda, muestra todas las transacciones
    } else {
        transactions.filter { transaction ->
            // Comprueba si el concepto contiene el searchQuery (ignorando mayúsculas/minúsculas)
            val matchesConcepto = transaction.concepto?.contains(searchQuery, ignoreCase = true) == true
            // Comprueba si el nombre de la categoría contiene el searchQuery (ignorando mayúsculas/minúsculas)
            val matchesCategory = transaction.categoryName.contains(searchQuery, ignoreCase = true)
            matchesConcepto || matchesCategory
        }
    }

    // Si después de filtrar no hay transacciones, muestra un mensaje específico
    if (filteredTransactions.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (searchQuery.isBlank()) "No hay transacciones registradas."
                else "No se encontraron transacciones para \"$searchQuery\".",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top=0.dp, bottom = 8.dp)
        ) {
            // Mostramos cada transacción filtrada
            items(filteredTransactions, key = { it.id }) { transactionItem ->
                TransactionRowItem(
                    transactionItem = transactionItem,
                    currency = currency,
                    onItemClick = { onTransactionClick(transactionItem.id) }
                )
            }
        }
    }
}

// Composable para mostrar un estado vacío cuando no hay transacciones
@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Aún no tienes transacciones registradas.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}