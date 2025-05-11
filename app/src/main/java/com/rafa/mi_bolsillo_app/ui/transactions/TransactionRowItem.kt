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
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.ui.theme.AppExpense // Asumiendo que definiste estos en Color.kt
import com.rafa.mi_bolsillo_app.ui.theme.AppIncome   // Asumiendo que definiste estos en Color.kt
import com.rafa.mi_bolsillo_app.ui.theme.MiBolsilloAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionRowItem(transactionItem: TransactionUiItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                CategoryColorIndicator(hexColor = transactionItem.categoryColorHex)
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transactionItem.description ?: transactionItem.categoryName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transactionItem.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(transactionItem.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (transactionItem.transactionType == TransactionType.INCOME) AppIncome else AppExpense
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatDate(transactionItem.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CategoryColorIndicator(hexColor: String, modifier: Modifier = Modifier) {
    val color = try {
        Color(android.graphics.Color.parseColor(hexColor))
    } catch (e: IllegalArgumentException) {
        MaterialTheme.colorScheme.surfaceVariant // Color por defecto si el hexadecimal es inválido
    }
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color)
    )
}

// Funciones de ayuda para formateo
private fun formatCurrency(amount: Double): String {
    // Implementa un formateo de moneda más robusto según sea necesario
    return String.format(Locale.getDefault(), "%.2f €", amount) // Ejemplo simple
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun TransactionRowItemPreview() {
    MiBolsilloAppTheme {
        TransactionRowItem(
            transactionItem = TransactionUiItem(
                id = 1,
                amount = 125.50,
                date = System.currentTimeMillis(),
                description = "Compra semanal supermercado",
                categoryName = "Comida",
                categoryColorHex = "#FFC107", // Amarillo para comida
                transactionType = TransactionType.EXPENSE
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionRowItemIncomePreview() {
    MiBolsilloAppTheme {
        TransactionRowItem(
            transactionItem = TransactionUiItem(
                id = 1,
                amount = 1500.0,
                date = System.currentTimeMillis(),
                description = "Salario Mayo",
                categoryName = "Salario",
                categoryColorHex = "#009688", // Verde azulado para salario
                transactionType = TransactionType.INCOME
            )
        )
    }
}