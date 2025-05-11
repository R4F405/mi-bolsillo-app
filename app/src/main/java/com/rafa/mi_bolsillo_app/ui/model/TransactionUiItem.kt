package com.rafa.mi_bolsillo_app.ui.model

import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType

data class TransactionUiItem(
    val id: Long,
    val amount: Double,
    val date: Long,
    // Este campo era 'description' en la entidad, pero en la UI lo llamamos 'concepto'
    // Mantenemos el nombre 'concepto' para la UI, pero recuerda que viene de 'transaction.description'
    val concepto: String?,
    val categoryName: String,
    val categoryColorHex: String,
    val transactionType: TransactionType
)