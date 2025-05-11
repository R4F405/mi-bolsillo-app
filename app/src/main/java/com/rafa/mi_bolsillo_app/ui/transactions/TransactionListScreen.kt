package com.rafa.mi_bolsillo_app.ui.transactions

import androidx.compose.foundation.isSystemInDarkTheme // ¡IMPORTANTE AÑADIR ESTE IMPORT!
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
    navController: NavController,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transactionsUiItems by viewModel.transactionsUiItems.collectAsStateWithLifecycle()
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
                title = { Text("Historial de Transacciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver" // Cambiado para ser más genérico
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

        Box(modifier = Modifier.padding(innerPadding)) {
            if (transactionsUiItems.isEmpty()) {
                EmptyState(modifier = Modifier.fillMaxSize())
            } else {
                TransactionList(
                    transactions = transactionsUiItems,
                    onTransactionClick = { transactionId ->
                        navController.navigate(AppScreens.AddTransactionScreen.createRoute(transactionId))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun TransactionList(
    transactions: List<TransactionUiItem>,
    onTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(), // Asegura que LazyColumn llene el espacio disponible
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(transactions, key = { it.id }) { transactionItem ->
            TransactionRowItem(
                transactionItem = transactionItem,
                onItemClick = { onTransactionClick(transactionItem.id) }
            )
        }
    }
}


@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(), // Asegura que EmptyState llene el espacio disponible
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No hay transacciones registradas.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant // Buen color para texto secundario
        )
    }
}