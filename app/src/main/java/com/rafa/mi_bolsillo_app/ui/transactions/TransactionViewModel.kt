package com.rafa.mi_bolsillo_app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
import com.rafa.mi_bolsillo_app.ui.model.TransactionUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine // Para combinar flows
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class para representar una transacción con su información de categoría para la UI
data class TransactionUiItem(
    val id: Long,
    val amount: Double,
    val date: Long,
    val description: String?,
    val categoryName: String,
    val categoryColorHex: String,
    val transactionType: TransactionType
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // ... (tus StateFlows _transactionsUiItems y _categories sin cambios) ...
    private val _transactionsUiItems = MutableStateFlow<List<TransactionUiItem>>(emptyList())
    val transactionsUiItems: StateFlow<List<TransactionUiItem>> = _transactionsUiItems.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()


    init {
        // Es importante llamar a seedInitialCategoriesIfNeeded ANTES de loadInitialData
        // si loadInitialData depende de que las categorías ya existan para el combine.
        viewModelScope.launch { // Lanzamos seed en su propia corrutina para que no bloquee
            seedInitialCategoriesIfNeeded()
            loadInitialData() // Ahora loadInitialData se llama después de que seed potencialmente complete
        }
    }

    private suspend fun seedInitialCategoriesIfNeeded() { // Marcada como suspend
        // Obtenemos la primera lista emitida por el Flow. Si la BD está vacía, será una lista vacía.
        val currentCategories = categoryRepository.getAllCategories().first()
        if (currentCategories.isEmpty()) {
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
            // No es necesario recolectar _categories aquí, loadInitialData ya lo hace.
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categoryList ->
                _categories.value = categoryList
                // Log para depurar
                println("ViewModel: Categorías cargadas en _categories: ${categoryList.size}")
            }
        }
        viewModelScope.launch {
            transactionRepository.getAllTransactions()
                .combine(_categories) { transactions, categories -> // Usamos el _categories ya poblado
                    val categoriesMap = categories.associateBy { it.id }
                    transactions.map { transaction -> // transaction es de tipo Transaction (tu entidad)
                        val category = categoriesMap[transaction.categoryId]
                        // Ahora creamos una instancia del TransactionUiItem importado desde ui.model
                        TransactionUiItem( // Esto llamará al constructor de com.rafa.mi_bolsillo_app.ui.model.TransactionUiItem
                            id = transaction.id,
                            amount = transaction.amount,
                            date = transaction.date,
                            concepto = transaction.description, // El 'description' de la entidad se mapea a 'concepto' en el UI item
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

    // La función addTransaction sigue usando 'description' internamente porque así se llama en la entidad Transaction.
    // El cambio a "Concepto" es solo a nivel de UI.
    fun addTransaction(amount: Double, date: Long, concepto: String?, categoryId: Long, type: TransactionType) {
        viewModelScope.launch {
            val newTransaction = Transaction(
                amount = amount,
                date = date,
                description = concepto, // Aquí se mapea 'concepto' de la UI a 'description' de la entidad
                categoryId = categoryId,
                transactionType = type
            )
            transactionRepository.insertTransaction(newTransaction)
        }
    }
}