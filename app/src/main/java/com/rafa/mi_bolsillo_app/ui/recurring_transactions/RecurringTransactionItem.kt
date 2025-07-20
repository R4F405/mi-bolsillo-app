package com.rafa.mi_bolsillo_app.ui.recurring_transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafa.mi_bolsillo_app.data.local.entity.RecurrenceFrequency
import com.rafa.mi_bolsillo_app.data.local.entity.RecurringTransaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.ui.theme.ExpenseRed
import com.rafa.mi_bolsillo_app.ui.theme.IncomeGreen
import com.rafa.mi_bolsillo_app.ui.theme.MiBolsilloAppTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Currency
import java.util.Locale

/**
 * Composable para mostrar un item de transacción recurrente.
 * Muestra el nombre, monto, categoría, frecuencia y detalles de la transacción.
 * Incluye botones para editar y eliminar la plantilla.
 *
 */

@Composable
fun RecurringTransactionItem(
    template: RecurringTransaction,
    currency: Currency,
    categoryName: String?,
    categoryColorHex: String?,
    onEditClick: (RecurringTransaction) -> Unit,
    onDeleteClick: (RecurringTransaction) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp)
            .clickable { onEditClick(template) }, // Hacer toda la tarjeta clickeable para editar
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Indicador de color de categoría
                    categoryColorHex?.let {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(it)))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    // Nombre de la plantilla
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                // Muestra el monto de la transacción
                Text(
                    text = formatCurrency(template.amount, currency),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (template.transactionType == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Muestra la categoría de la transacción
            Text(
                text = categoryName ?: "Categoría no encontrada",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Descripción de la plantilla, si existe
            template.description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Descripción: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Frecuencia de la transacción recurrente
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Autorenew,
                    contentDescription = "Frecuencia",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatFrequency(template.frequency, template.interval, template.dayOfMonth, template.monthOfYear),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            // Proximas ocurrencias
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Próxima: ${formatDate(template.nextOccurrenceDate, "dd/MM/yy HH:mm")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Fecha de finalización
            template.endDate?.let {
                Text(
                    text = "Finaliza: ${formatDate(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Icon de estado activo/inactivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = if (template.isActive) Icons.Filled.PlayCircleOutline else Icons.Filled.PauseCircleOutline,
                    contentDescription = if (template.isActive) "Activa" else "Inactiva",
                    tint = if (template.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )

                // Botones de acción: Editar y Eliminar
                Row {
                    IconButton(onClick = { onEditClick(template) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Edit, "Editar plantilla", tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { onDeleteClick(template) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Delete, "Eliminar plantilla", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// Formato de moneda
fun formatCurrency(amount: Double, currency: Currency): String {
    return NumberFormat.getCurrencyInstance().apply {
        this.currency = currency
    }.format(amount)
}

// Formato de fecha
fun formatDate(timestamp: Long?, pattern: String = "dd MMM yyyy"): String {
    return if (timestamp != null) {
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    } else {
        "N/A"
    }
}

// Formatea la frecuencia de recurrencia en un texto legible
fun formatFrequency(
    frequency: RecurrenceFrequency,
    interval: Int,
    dayOfMonth: Int?,
    monthOfYear: Int?
): String {
    val prefix = "Cada "
    val intervalText = if (interval > 1) "$interval " else ""
    val baseText = when (frequency) {
        RecurrenceFrequency.DAILY -> if (interval > 1) "días" else "día"
        RecurrenceFrequency.WEEKLY -> if (interval > 1) "semanas" else "semana"
        RecurrenceFrequency.MONTHLY -> {
            val daySuffix = dayOfMonth?.let { " (el día $it)" } ?: ""
            if (interval > 1) "meses$daySuffix" else "mes$daySuffix"
        }
        RecurrenceFrequency.YEARLY -> if (interval > 1) "años" else "año"
    }
    return prefix + intervalText + baseText
}

// Preview para el item de transacción recurrente
@Preview(showBackground = true)
@Composable
fun RecurringTransactionItemPreview() {
    MiBolsilloAppTheme {
        RecurringTransactionItem(
            template = RecurringTransaction(
                id = 1,
                name = "Suscripción Mensual App Música",
                amount = 9.99,
                description = "Spotify Premium Familiar",
                categoryId = 1,
                transactionType = TransactionType.EXPENSE,
                startDate = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000,
                frequency = RecurrenceFrequency.MONTHLY,
                interval = 1,
                dayOfMonth = 15,
                monthOfYear = null,
                endDate = null,
                nextOccurrenceDate = System.currentTimeMillis() + 15L * 24 * 60 * 60 * 1000,
                lastGeneratedDate = System.currentTimeMillis() - 15L * 24 * 60 * 60 * 1000,
                isActive = true
            ),
            currency = Currency.getInstance("EUR"),
            categoryName = "Entretenimiento",
            categoryColorHex = "#E91E63", // Rosa
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}