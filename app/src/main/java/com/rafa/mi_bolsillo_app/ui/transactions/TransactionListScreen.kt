package com.rafa.mi_bolsillo_app.ui.transactions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Icono de flecha atrás
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // Para el botón de navegación
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController // Necesario para la navegación
import com.rafa.mi_bolsillo_app.navigation.AppScreens // Para las rutas
import com.rafa.mi_bolsillo_app.ui.model.TransactionUiItem // Asegúrate que el import es a ui.model

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    navController: NavController, // Ya lo teníamos para la navegación
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transactionsUiItems by viewModel.transactionsUiItems.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Transacciones") }, // Título actualizado
                navigationIcon = { // Icono para volver atrás
                    IconButton(onClick = { navController.navigateUp() }) { // O navController.popBackStack()
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al Dashboard"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
        // Eliminamos el FloatingActionButton de aquí si el principal está en el Dashboard
        // floatingActionButton = { ... }
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding)) {
            if (transactionsUiItems.isEmpty()) {
                EmptyState(modifier = Modifier.fillMaxSize())
            } else {
                TransactionList(
                    transactions = transactionsUiItems,
                    onTransactionClick = { transactionId ->
                        // Navegar a la pantalla de edición/creación pasando el ID
                        // Usaremos la misma pantalla AddTransactionScreen pero con un ID
                        navController.navigate("${AppScreens.AddTransactionScreen.route}?transactionId=$transactionId")
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        // El ModalBottomSheet para añadir transacciones se eliminó de aquí
    }
}

@Composable
fun TransactionList(
    transactions: List<TransactionUiItem>,
    onTransactionClick: (Long) -> Unit, // Callback para el clic en un ítem
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(transactions, key = { it.id }) { transactionItem ->
            TransactionRowItem(
                transactionItem = transactionItem,
                onItemClick = { onTransactionClick(transactionItem.id) } // Pasar el ID
            )
        }
    }
}


@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No hay transacciones registradas.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}