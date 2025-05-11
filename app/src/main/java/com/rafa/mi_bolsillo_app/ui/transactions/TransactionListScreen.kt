package com.rafa.mi_bolsillo_app.ui.transactions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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

@OptIn(ExperimentalMaterial3Api::class) // Para TopAppBar y Scaffold
@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = hiltViewModel(),
    onAddTransactionClick: () -> Unit = {} // Callback para cuando se pulse el FAB
) {
    // Recolectamos el estado de transactionsUiItems del ViewModel.
    // collectAsStateWithLifecycle se encarga de observar el Flow de forma segura respecto al ciclo de vida del Composable.
    val transactionsUiItems by viewModel.transactionsUiItems.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Transacciones") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransactionClick) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir transacción")
            }
        }
    ) { innerPadding -> // innerPadding es proporcionado por Scaffold para evitar solapamientos

        if (transactionsUiItems.isEmpty()) {
            EmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            TransactionList(
                transactions = transactionsUiItems,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun TransactionList(
    transactions: List<TransactionUiItem>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(transactions, key = { it.id }) { transactionItem ->
            TransactionRowItem(transactionItem = transactionItem)
            // Divider() // para añadir un divisor entre items
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