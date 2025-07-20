package com.rafa.mi_bolsillo_app.ui.transactions

import com.rafa.mi_bolsillo_app.ui.model.TransactionUiItem
import java.util.Currency

/**
 * Modelo de datos para la pantalla de lista de transacciones.
 *
 * Contiene la lista de transacciones y la moneda utilizada.
 */

data class TransactionListUiState(
    val transactions: List<TransactionUiItem> = emptyList(),
    val currency: Currency = Currency.getInstance("EUR")
)