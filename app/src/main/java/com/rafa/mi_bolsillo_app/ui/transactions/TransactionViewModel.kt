package com.rafa.mi_bolsillo_app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
import com.rafa.mi_bolsillo_app.ui.model.TransactionUiItem // Asegúrate que el import es a ui.model
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _transactionsUiItems = MutableStateFlow<List<TransactionUiItem>>(emptyList())
    val transactionsUiItems: StateFlow<List<TransactionUiItem>> = _transactionsUiItems.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    // Nuevo: StateFlow para la transacción que se está editando
    private val _transactionToEdit = MutableStateFlow<Transaction?>(null)
    val transactionToEdit: StateFlow<Transaction?> = _transactionToEdit.asStateFlow()

    init {
        viewModelScope.launch {
            seedInitialCategoriesIfNeeded()
            loadInitialData()
        }
    }

    private suspend fun seedInitialCategoriesIfNeeded() {
        val currentCategories = categoryRepository.getAllCategories().first()
        if (currentCategories.isEmpty()) {
            // ... (tu lista de predefinedCategories sin cambios)
            val predefinedCategories = listOf(
                Category(name = "Comida", iconName = "ic_food", colorHex = "#FFC107", isPredefined = true),
                Category(name = "Transporte", iconName = "ic_transport", colorHex = "#2196F3", isPredefined = true),
                Category(name = "Ocio", iconName = "ic_leisure", colorHex = "#4CAF50", isPredefined = true),
                Category(name = "Hogar", iconName = "ic_home", colorHex = "#E91E63", isPredefined = true),
                Category(name = "Salario", iconName = "ic_salary", colorHex = "#009688", isPredefined = true),
                Category(name = "Otros Ingresos", iconName = "ic_other_income", colorHex = "#795548", isPredefined = true),
                Category(name = "Sin Categoría", iconName = "ic_no_category", colorHex = "#9E9E9E", isPredefined = true)
            )
            categoryRepository.insertCategories(predefinedCategories)
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categoryList ->
                _categories.value = categoryList
            }
        }
        viewModelScope.launch {
            // Necesitamos las categorías para mapear Transaction a TransactionUiItem
            // Asegurémonos de que _categories se combine correctamente.
            transactionRepository.getAllTransactions()
                .combine(_categories) { transactions, categoriesList ->
                    val categoriesMap = categoriesList.associateBy { it.id }
                    transactions.map { transaction ->
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
                }.collect { combinedList ->
                    _transactionsUiItems.value = combinedList
                }
        }
    }

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

    // Nueva función para cargar una transacción para edición
    fun loadTransactionForEditing(transactionId: Long) {
        viewModelScope.launch {
            // Si transactionId es -1L (o nuestro valor por defecto para "nueva"), no cargamos nada.
            if (transactionId == -1L) {
                _transactionToEdit.value = null // Aseguramos que esté limpio para una nueva transacción
                return@launch
            }
            val transaction = transactionRepository.getTransactionById(transactionId)
            _transactionToEdit.value = transaction
            // AddTransactionScreen observará transactionToEdit y actualizará sus campos.
        }
    }

    // Nueva función para actualizar una transacción existente
    fun updateTransaction(
        transactionId: Long, // Importante pasar el ID original
        amount: Double,
        date: Long,
        concepto: String?,
        categoryId: Long,
        type: TransactionType
    ) {
        viewModelScope.launch {
            if (transactionId == -1L) return@launch // No debería ocurrir si la lógica es correcta

            val updatedTransaction = Transaction(
                id = transactionId, // <-- USA EL ID ORIGINAL para la actualización
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

    // Nueva función para limpiar el estado de edición
    fun clearEditingTransaction() {
        _transactionToEdit.value = null
    }
}