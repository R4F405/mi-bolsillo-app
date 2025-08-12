package com.rafa.mi_bolsillo_app.ui.data_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel para la pantalla de Gestión de Datos.
 * Proporciona la lógica de negocio para exportar e importar transacciones.
 */

data class DataManagementUiState(
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DataManagementUiState())
    val uiState: StateFlow<DataManagementUiState> = _uiState.asStateFlow()

    // Definimos el formato de fecha que usaremos para la importación/exportación
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    /**
     * Prepara el contenido CSV para exportar.
     * @return Una cadena con todas las transacciones en formato CSV.
     */
    suspend fun getCsvContent(): String {
        val transactions = transactionRepository.getAllTransactionsList()
        val categories = categoryRepository.getAllCategories().first().associateBy { it.id }

        val csvContent = StringBuilder()
        // Cabecera del CSV
        csvContent.append("Date,Amount,Description,Category,TransactionType\n")

        // Filas de transacciones
        transactions.forEach { transaction ->
            // Convertimos el timestamp (Long) a una fecha formateada (String)
            val formattedDate = dateFormat.format(Date(transaction.date))
            val categoryName = categories[transaction.categoryId]?.name ?: "Sin Categoría"
            // Se asegura de que las descripciones con comas estén entre comillas
            val description = transaction.description?.replace("\"", "\"\"") ?: ""
            val line = "\"$formattedDate\",${transaction.amount},\"$description\",\"$categoryName\",${transaction.transactionType}\n"
            csvContent.append(line)
        }
        return csvContent.toString()
    }

    /**
     * Importa transacciones desde una cadena en formato CSV.
     * @param csvContent El contenido del archivo CSV.
     */
    fun importTransactionsFromCsv(csvContent: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val lines = csvContent.lines().drop(1) // Omitir la cabecera
                if (lines.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, userMessage = "El archivo CSV está vacío o no tiene datos.") }
                    return@launch
                }

                val existingCategories = categoryRepository.getAllCategories().first()
                    .associateBy { it.name.lowercase() }
                    .toMutableMap()

                val newTransactions = mutableListOf<Transaction>()

                for (line in lines) {
                    if (line.isBlank()) continue

                    // Un parser más robusto que maneja comas dentro de las descripciones
                    val parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                    if (parts.size < 5) continue

                    // Parseamos la fecha (String) para convertirla en timestamp (Long)
                    val dateString = parts[0].removeSurrounding("\"")
                    val date = try { dateFormat.parse(dateString)?.time } catch (e: Exception) { null }
                    val amount = parts[1].toDoubleOrNull()
                    val description = parts[2].removeSurrounding("\"")
                    val categoryName = parts[3].removeSurrounding("\"").trim()
                    val type = try { TransactionType.valueOf(parts[4].trim()) } catch (e: Exception) { null }

                    if (date != null && amount != null && type != null && categoryName.isNotBlank()) {
                        var category = existingCategories[categoryName.lowercase()]

                        if (category == null) {
                            val newCategory = Category(name = categoryName, colorHex = "#CCCCCC", isPredefined = false)
                            val newId = categoryRepository.insertCategory(newCategory)
                            category = newCategory.copy(id = newId)
                            existingCategories[categoryName.lowercase()] = category
                        }

                        newTransactions.add(
                            Transaction(
                                date = date,
                                amount = amount,
                                description = description,
                                categoryId = category.id,
                                transactionType = type
                            )
                        )
                    }
                }

                // Insertar transacciones en la base de datos
                newTransactions.forEach { transaction ->
                    transactionRepository.insertTransaction(transaction)
                }

                _uiState.update { it.copy(isLoading = false, userMessage = "${newTransactions.size} transacciones importadas correctamente.") }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, userMessage = "Error al importar el archivo: ${e.message}") }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}