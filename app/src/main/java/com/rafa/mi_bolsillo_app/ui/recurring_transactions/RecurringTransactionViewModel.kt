package com.rafa.mi_bolsillo_app.ui.recurring_transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.RecurrenceFrequency
import com.rafa.mi_bolsillo_app.data.local.entity.RecurringTransaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import com.rafa.mi_bolsillo_app.data.repository.RecurringTransactionRepository
import com.rafa.mi_bolsillo_app.utils.RecurrenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringTransactionScreenUiState(
    val recurringTransactions: List<RecurringTransaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val recurringTransactionToEdit: RecurringTransaction? = null,
    val showEditSheet: Boolean = false
)

@HiltViewModel
class RecurringTransactionViewModel @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val categoryRepository: CategoryRepository // Para el selector de categorías
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringTransactionScreenUiState())
    val uiState: StateFlow<RecurringTransactionScreenUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(
                recurringTransactionRepository.getActiveRecurringTransactionsSortedByNextOccurrence(),
                categoryRepository.getAllCategories()
            ) { templates, cats ->
                RecurringTransactionScreenUiState(
                    recurringTransactions = templates,
                    categories = cats,
                    isLoading = false
                )
            }.collect { combinedState ->
                _uiState.value = combinedState
            }
        }
    }

    fun prepareForEditing(template: RecurringTransaction?) {
        _uiState.update {
            it.copy(
                recurringTransactionToEdit = template,
                showEditSheet = true,
                userMessage = null // Limpiar mensajes antiguos
            )
        }
    }

    fun dismissEditSheet() {
        _uiState.update { it.copy(showEditSheet = false, recurringTransactionToEdit = null) }
    }

    fun saveRecurringTransaction(
        id: Long?, // null si es nueva
        name: String,
        amount: Double,
        description: String?,
        categoryId: Long,
        transactionType: TransactionType,
        startDate: Long,
        frequency: RecurrenceFrequency,
        interval: Int,
        dayOfMonth: Int?,
        monthOfYear: Int?,
        endDate: Long?,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _uiState.update { it.copy(userMessage = "El nombre de la plantilla es obligatorio.") }
                return@launch
            }
            if (amount <= 0) {
                _uiState.update { it.copy(userMessage = "El monto debe ser positivo.") }
                return@launch
            }
            if (interval <= 0) {
                _uiState.update { it.copy(userMessage = "El intervalo debe ser positivo.") }
                return@launch
            }

            // Calcular la primera 'nextOccurrenceDate'
            // Si es una nueva plantilla, la primera nextOccurrenceDate es la startDate.
            // Si se está editando, y la startDate ha cambiado y es futura a la actual nextOccurrenceDate,
            // O si la frecuencia/intervalo cambian, se debería recalcular.
            // Por simplicidad inicial: si es nueva, es startDate. Si se edita, se podría mantener la existente
            // o recalcularla si la startDate cambió significativamente.
            // Para esta versión, la primera nextOccurrenceDate será siempre la startDate si no se está editando o
            // si se está editando y la start date es mayor que la actual nextOccurrenceDate
            var nextOccurrence = startDate
            if (id != null) { // Editando
                val currentTemplate = _uiState.value.recurringTransactionToEdit
                if (currentTemplate != null) {
                    nextOccurrence = if(startDate > currentTemplate.nextOccurrenceDate) startDate else currentTemplate.nextOccurrenceDate

                    // Una lógica más robusta recalcularía si las reglas de frecuencia cambian
                    if(currentTemplate.startDate != startDate ||
                        currentTemplate.frequency != frequency ||
                        currentTemplate.interval != interval ||
                        currentTemplate.dayOfMonth != dayOfMonth ||
                        currentTemplate.monthOfYear != monthOfYear) {
                        // Si algo clave de la recurrencia cambió, recalcular desde startDate
                        var tempNextOccurrence = startDate
                        // Nos aseguramos que la próxima ocurrencia no sea en el pasado si startDate es antigua
                        while (tempNextOccurrence < System.currentTimeMillis() && (endDate == null || tempNextOccurrence <= endDate)) {
                            tempNextOccurrence = RecurrenceHelper.calculateNextOccurrenceDate(
                                tempNextOccurrence, frequency, interval, dayOfMonth, monthOfYear
                            )
                        }
                        nextOccurrence = tempNextOccurrence
                    }
                }
            } else { // Nueva, o si no se encontró la plantilla a editar (no debería pasar)
                // Si startDate es en el pasado, calcular la primera ocurrencia futura o igual a hoy
                var tempNextOccurrence = startDate
                while (tempNextOccurrence < System.currentTimeMillis() && (endDate == null || tempNextOccurrence <= endDate)) {
                    tempNextOccurrence = RecurrenceHelper.calculateNextOccurrenceDate(
                        tempNextOccurrence, frequency, interval, dayOfMonth, monthOfYear
                    )
                }
                nextOccurrence = tempNextOccurrence
            }


            val template = RecurringTransaction(
                id = id ?: 0,
                name = name,
                amount = amount,
                description = description,
                categoryId = categoryId,
                transactionType = transactionType,
                startDate = startDate,
                frequency = frequency,
                interval = interval,
                dayOfMonth = dayOfMonth,
                monthOfYear = monthOfYear,
                endDate = endDate,
                nextOccurrenceDate = nextOccurrence, // Calculada
                lastGeneratedDate = _uiState.value.recurringTransactionToEdit?.lastGeneratedDate, // Mantener si se edita
                isActive = isActive,
                creationDate = _uiState.value.recurringTransactionToEdit?.creationDate ?: System.currentTimeMillis()
            )

            if (id == null) {
                recurringTransactionRepository.insertRecurringTransaction(template)
                _uiState.update { it.copy(userMessage = "Plantilla recurrente creada.", showEditSheet = false) }
            } else {
                recurringTransactionRepository.updateRecurringTransaction(template)
                _uiState.update { it.copy(userMessage = "Plantilla recurrente actualizada.", showEditSheet = false) }
            }
            // La lista se actualizará por el Flow en loadInitialData.
        }
    }

    fun deleteRecurringTransaction(templateId: Long) {
        viewModelScope.launch {
            val template = recurringTransactionRepository.getRecurringTransactionById(templateId)
            template?.let {
                recurringTransactionRepository.deleteRecurringTransaction(it)
                _uiState.update { state -> state.copy(userMessage = "Plantilla '${it.name}' eliminada.") }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}