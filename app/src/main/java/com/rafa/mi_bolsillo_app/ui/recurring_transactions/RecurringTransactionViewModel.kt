package com.rafa.mi_bolsillo_app.ui.recurring_transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.RecurrenceFrequency
import com.rafa.mi_bolsillo_app.data.local.entity.RecurringTransaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import com.rafa.mi_bolsillo_app.data.repository.RecurringTransactionRepository
import com.rafa.mi_bolsillo_app.data.repository.SettingsRepository
import com.rafa.mi_bolsillo_app.utils.RecurrenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Currency
import javax.inject.Inject

/**
 * ViewModel para la pantalla de transacciones recurrentes.
 * Proporciona la lógica de negocio para manejar transacciones recurrentes, categorías y configuración de moneda.
 * Incluye operaciones para crear, editar, eliminar y listar transacciones recurrentes,
 * así como para manejar el estado de la UI y mensajes al usuario.
 */

// Modelo para el estado de la pantalla de transacciones recurrentes
data class RecurringTransactionScreenUiState(
    val recurringTransactions: List<RecurringTransaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val recurringTransactionToEdit: RecurringTransaction? = null,
    val showEditSheet: Boolean = false,
    val currency: Currency = Currency.getInstance("EUR")
)

@HiltViewModel
class RecurringTransactionViewModel @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringTransactionScreenUiState())
    val uiState: StateFlow<RecurringTransactionScreenUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    // Carga inicial de datos
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(
                recurringTransactionRepository.getActiveRecurringTransactionsSortedByNextOccurrence(),
                categoryRepository.getAllCategories(),
                settingsRepository.currency
            ) { templates, cats, currency ->
                RecurringTransactionScreenUiState(
                    recurringTransactions = templates,
                    categories = cats,
                    isLoading = false,
                    currency = currency
                )
            }.collect { combinedState ->
                _uiState.value = combinedState.copy(
                    // Mantenemos el estado del diálogo de edición al refrescar los datos
                    showEditSheet = _uiState.value.showEditSheet,
                    recurringTransactionToEdit = _uiState.value.recurringTransactionToEdit,
                    userMessage = _uiState.value.userMessage
                )
            }
        }
    }

    // Prepara el estado para editar una transacción recurrente
    fun prepareForEditing(template: RecurringTransaction?) {
        _uiState.update {
            it.copy(
                recurringTransactionToEdit = template,
                showEditSheet = true,
                userMessage = null // Limpiar mensajes antiguos
            )
        }
    }

    // Cierra el diálogo de edición
    fun dismissEditSheet() {
        _uiState.update { it.copy(showEditSheet = false, recurringTransactionToEdit = null) }
    }

    // Guarda una transacción recurrente, ya sea nueva o editada
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

            /*
            Calcular la primera 'nextOccurrenceDate'
            Si es una nueva plantilla, la primera nextOccurrenceDate es la startDate.
            Si se está editando, y la startDate ha cambiado y es futura a la actual nextOccurrenceDate,
            o si la frecuencia/intervalo cambian, se debería recalcular.
             */

            var nextOccurrence = startDate
            if (id != null) {
                val currentTemplate = recurringTransactionRepository.getRecurringTransactionById(id)
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
            } else { // Nueva, o si no se encontró la plantilla a editar
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
                name = name, amount = amount, description = description, categoryId = categoryId,
                transactionType = transactionType, startDate = startDate, frequency = frequency,
                interval = interval, dayOfMonth = dayOfMonth, monthOfYear = monthOfYear,
                endDate = endDate, nextOccurrenceDate = nextOccurrence,
                lastGeneratedDate = if (id != null) recurringTransactionRepository.getRecurringTransactionById(id)?.lastGeneratedDate else null,
                isActive = isActive,
                creationDate = if (id != null) recurringTransactionRepository.getRecurringTransactionById(id)?.creationDate ?: System.currentTimeMillis() else System.currentTimeMillis()
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

    // Elimina una transacción recurrente por su ID
    fun deleteRecurringTransaction(templateId: Long) {
        viewModelScope.launch {
            val template = recurringTransactionRepository.getRecurringTransactionById(templateId)
            template?.let {
                recurringTransactionRepository.deleteRecurringTransaction(it)
                _uiState.update { state -> state.copy(userMessage = "Plantilla '${it.name}' eliminada.") }
            }
        }
    }

    // Limpia el mensaje de usuario actual
    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}