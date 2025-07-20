package com.rafa.mi_bolsillo_app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import com.rafa.mi_bolsillo_app.data.repository.SettingsRepository
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
import com.rafa.mi_bolsillo_app.ui.model.TransactionUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de transacciones.
 * Proporciona la lógica de negocio para manejar transacciones, categorías y configuración de moneda.
 * Incluye operaciones para agregar, editar, eliminar y listar transacciones,
 * así como para manejar el estado de la UI y mensajes al usuario.
 *
 */

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _transactionToEdit = MutableStateFlow<Transaction?>(null)
    val transactionToEdit: StateFlow<Transaction?> = _transactionToEdit.asStateFlow()

    init {
        viewModelScope.launch {
            seedInitialCategoriesIfNeeded()
            loadInitialData()
        }
    }

    //Lista de categorías predefinidas
    private suspend fun seedInitialCategoriesIfNeeded() {
        val currentCategories = categoryRepository.getAllCategories().first()
        if (currentCategories.isEmpty()) {
            val predefinedCategories = listOf(
                Category(name = "Comida", colorHex = "#FFC107", isPredefined = true),
                Category(name = "Transporte", colorHex = "#2196F3", isPredefined = true),
                Category(name = "Ocio", colorHex = "#4CAF50", isPredefined = true),
                Category(name = "Hogar", colorHex = "#E91E63", isPredefined = true),
                Category(name = "Salario", colorHex = "#009688", isPredefined = true),
                Category(name = "Otros Ingresos", colorHex = "#795548", isPredefined = true),
                Category(name = "Sin Categoría", colorHex = "#9E9E9E", isPredefined = true)
            )
            categoryRepository.insertCategories(predefinedCategories)
        }
    }

    // Carga inicial de datos
    private fun loadInitialData() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categoryList ->
                _categories.value = categoryList
            }
        }
        viewModelScope.launch {
            // Combinamos los tres flujos: transacciones, categorías y moneda
            combine(
                transactionRepository.getAllTransactions(),
                _categories,
                settingsRepository.currency
            ) { transactions, categoriesList, currency ->
                    val categoriesMap = categoriesList.associateBy { it.id }
                    val transactionUiItems = transactions.map { transaction ->
                        val category = categoriesMap[transaction.categoryId]
                        TransactionUiItem(
                            id = transaction.id,
                            amount = transaction.amount,
                            date = transaction.date,
                            concepto = transaction.description,
                            categoryName = category?.name ?: "Sin Categoría",
                            categoryColorHex = category?.colorHex ?: "#808080",
                            transactionType = transaction.transactionType
                        )
                    }
                    TransactionListUiState(
                        transactions = transactionUiItems,
                        currency = currency
                    )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    // Función para agregar una transacción
    fun addTransaction(amount: Double, date: Long, concepto: String?, categoryId: Long, type: TransactionType) {
        viewModelScope.launch {
            val newTransaction = Transaction(
                // id se autogenerará por Room
                amount = amount,
                date = date,
                description = concepto,
                categoryId = categoryId,
                transactionType = type
            )
            transactionRepository.insertTransaction(newTransaction)
        }
    }

    // Función para cargar una transacción para edición
    fun loadTransactionForEditing(transactionId: Long) {
        viewModelScope.launch {
            // Si transactionId es -1L, no cargamos nada.
            if (transactionId == -1L) {
                _transactionToEdit.value = null // Aseguramos que esté limpio para una nueva transacción
                return@launch
            }
            val transaction = transactionRepository.getTransactionById(transactionId)
            _transactionToEdit.value = transaction
            // AddTransactionScreen observará transactionToEdit y actualizará sus campos.
        }
    }

    // Función para actualizar una transacción existente
    fun updateTransaction(
        transactionId: Long,
        amount: Double,
        date: Long,
        concepto: String?,
        categoryId: Long,
        type: TransactionType
    ) {
        viewModelScope.launch {
            if (transactionId == -1L) return@launch

            val updatedTransaction = Transaction(
                id = transactionId,
                amount = amount,
                date = date,
                description = concepto,
                categoryId = categoryId,
                transactionType = type
            )
            transactionRepository.updateTransaction(updatedTransaction)
            _transactionToEdit.value = null // Limpiar después de editar
        }
    }

    // Función para eliminar una transacción
    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            if (transactionId != -1L) { // Asegurarse de que no sea el ID por defecto
                val transactionToDelete = transactionRepository.getTransactionById(transactionId)
                transactionToDelete?.let {
                    transactionRepository.deleteTransaction(it)
                    _transactionToEdit.value = null // Limpiar el estado de edición también
                }
            }
        }
    }

    // Función para limpiar el estado de edición
    fun clearEditingTransaction() {
        _transactionToEdit.value = null
    }
}