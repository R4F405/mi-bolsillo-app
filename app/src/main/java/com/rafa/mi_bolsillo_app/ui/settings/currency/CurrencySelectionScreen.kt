package com.rafa.mi_bolsillo_app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentCurrency by viewModel.currentCurrency.collectAsState()
    val currentDarkTheme = isSystemInDarkTheme()

    // Lista de monedas comunes. Puedes expandirla.
    val availableCurrencies = remember {
        listOf(
            Currency.getInstance("EUR"), // Euro
            Currency.getInstance("USD"), // US Dollar
            Currency.getInstance("GBP"), // British Pound
            Currency.getInstance("JPY"), // Japanese Yen
            Currency.getInstance("MXN")  // Mexican Peso
        ).sortedBy { it.displayName }
    }

    Scaffold(
        topBar = {
            val topAppBarContainerColor = if (currentDarkTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
            val topAppBarContentColor = if (currentDarkTheme) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
            TopAppBar(
                title = { Text("Seleccionar Moneda") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topAppBarContainerColor,
                    titleContentColor = topAppBarContentColor,
                    navigationIconContentColor = topAppBarContentColor,
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(availableCurrencies) { currency ->
                CurrencyItem(
                    currency = currency,
                    isSelected = currency.currencyCode == currentCurrency.currencyCode,
                    onCurrencySelected = {
                        viewModel.saveCurrency(it.currencyCode)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun CurrencyItem(
    currency: Currency,
    isSelected: Boolean,
    onCurrencySelected: (Currency) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCurrencySelected(currency) }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currency.getDisplayName(Locale.getDefault()),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${currency.currencyCode} (${currency.symbol})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Seleccionada",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}