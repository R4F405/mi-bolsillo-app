package com.rafa.mi_bolsillo_app.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafa.mi_bolsillo_app.ui.model.TransactionUiItem
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.ui.theme.ExpenseRed // Cambio
import com.rafa.mi_bolsillo_app.ui.theme.IncomeGreen // Cambio
import com.rafa.mi_bolsillo_app.ui.theme.MiBolsilloAppTheme
import androidx.compose.foundation.clickable
import com.rafa.mi_bolsillo_app.ui.recurring_transactions.formatCurrency
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale


// Composable para mostrar una fila de una transacción
@Composable
fun TransactionRowItem(
    transactionItem: TransactionUiItem,
    currency: Currency,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onItemClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Indicador de color de categoría
                CategoryColorIndicator(hexColor = transactionItem.categoryColorHex)
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transactionItem.concepto ?: transactionItem.categoryName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    // Nombre de la categoría
                    Text(
                        text = transactionItem.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
            // Monto de la transacción
                Text(
                    text = formatCurrency(transactionItem.amount, currency),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (transactionItem.transactionType == TransactionType.INCOME) IncomeGreen else ExpenseRed
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Fecha de la transacción
                Text(
                    text = formatDate(transactionItem.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Composable para mostrar un indicador de color de categoría
@Composable
fun CategoryColorIndicator(hexColor: String, modifier: Modifier = Modifier) {
    val color = try {
        Color(android.graphics.Color.parseColor(hexColor))
    } catch (e: IllegalArgumentException) {
        MaterialTheme.colorScheme.surfaceVariant
    }
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color)
    )
}

// Formatea la fecha de la transacción a un formato legible
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Composable de vista previa
@Preview(showBackground = true)
@Composable
fun TransactionRowItemPreview() {
    MiBolsilloAppTheme {
        TransactionRowItem(
            transactionItem = TransactionUiItem(
                id = 1,
                amount = 125.50,
                date = System.currentTimeMillis(),
                concepto  = "Compra semanal supermercado",
                categoryName = "Comida",
                categoryColorHex = "#FFC107",
                transactionType = TransactionType.EXPENSE
            ),
            currency = Currency.getInstance("EUR"),
            onItemClick = {} // Añadir lambda vacía para el preview
        )
    }
}

// Composable de vista previa para ingresos
@Preview(showBackground = true)
@Composable
fun TransactionRowItemIncomePreview() {
    MiBolsilloAppTheme {
        TransactionRowItem(
            transactionItem = TransactionUiItem(
                id = 1,
                amount = 1500.0,
                date = System.currentTimeMillis(),
                concepto = "Salario Mayo",
                categoryName = "Salario",
                categoryColorHex = "#009688",
                transactionType = TransactionType.INCOME
            ),
            currency = Currency.getInstance("EUR"),
            onItemClick = {} // Añadir lambda vacía para el preview
        )
    }
}