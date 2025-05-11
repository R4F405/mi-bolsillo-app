package com.rafa.mi_bolsillo_app.ui.transactions

import androidx.navigation.NavController
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import com.rafa.mi_bolsillo_app.ui.model.TransactionUiItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    navController: NavController,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transactionsUiItems by viewModel.transactionsUiItems.collectAsStateWithLifecycle()

    // Estado para controlar la visibilidad del ModalBottomSheet
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    // Estado para el ModalBottomSheet
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Para que solo sea expandido o oculto
    )
    val scope = rememberCoroutineScope() // Coroutine scope para lanzar operaciones del sheet

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
            FloatingActionButton(onClick = {
                scope.launch {
                    showBottomSheet = true // Mostrar el BottomSheet
                }
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir transacción")
            }
        }
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding)) { // Usamos Box para que el contenido principal y el sheet no se solapen de forma inesperada
            if (transactionsUiItems.isEmpty()) {
                EmptyState(modifier = Modifier.fillMaxSize())
            } else {
                TransactionList(
                    transactions = transactionsUiItems,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Definir el ModalBottomSheet
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState,
            ) {
                AddTransactionSheetContent(
                    viewModel = viewModel,
                    onTransactionAdded = {
                        // Acción a realizar después de añadir la transacción y cerrar el sheet
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }
                )
            }
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
        // Ahora 'transactionItem' aquí será del tipo ui.model.TransactionUiItem
        items(transactions, key = { it.id }) { transactionItem ->
            TransactionRowItem(transactionItem = transactionItem)
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