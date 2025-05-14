package com.rafa.mi_bolsillo_app.ui.model

import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType

/**
 * Modelo de datos para representar una transacción en la UI.
 *
 * Esta clase se utiliza para representar una transacción en la UI.
 */

data class TransactionUiItem(
    val id: Long,
    val amount: Double,
    val date: Long,
    val concepto: String?,
    val categoryName: String,
    val categoryColorHex: String,
    val transactionType: TransactionType
)