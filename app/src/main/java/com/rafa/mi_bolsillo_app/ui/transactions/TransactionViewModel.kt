package com.rafa.mi_bolsillo_app.ui.transactions

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
import kotlinx.coroutines.flow.combine // Para combinar flows
import kotlinx.coroutines.flow.firstOrNull
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
    private val categoryRepository: CategoryRepository // Inyectamos ambos repositorios
) : ViewModel() {

    // StateFlow para la lista de transacciones (combinadas con info de categoría)
    // Usamos un MutableStateFlow privado para la emisión interna
    private val _transactionsUiItems = MutableStateFlow<List<TransactionUiItem>>(emptyList())
    // Y un StateFlow público e inmutable para la observación desde la UI
    val transactionsUiItems: StateFlow<List<TransactionUiItem>> = _transactionsUiItems.asStateFlow()

    // StateFlow para la lista de categorías (para el formulario de añadir transacción)
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Observar cambios en transacciones y categorías y combinarlos
            transactionRepository.getAllTransactions()
                .combine(categoryRepository.getAllCategories()) { transactions, categories ->
                    // Crear un mapa de ID de categoría a categoría para búsqueda rápida
                    val categoriesMap = categories.associateBy { it.id }
                    transactions.map { transaction ->
                        val category = categoriesMap[transaction.categoryId]
                        TransactionUiItem(
                            id = transaction.id,
                            amount = transaction.amount,
                            date = transaction.date,
                            description = transaction.description,
                            categoryName = category?.name ?: "Sin Categoría", // Nombre por defecto
                            categoryColorHex = category?.colorHex ?: "#808080", // Color gris por defecto
                            transactionType = transaction.transactionType
                        )
                    }
                }.collect { combinedList ->
                    _transactionsUiItems.value = combinedList
                }
        }

        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categoryList ->
                _categories.value = categoryList
            }
        }
    }

    // Función para añadir una nueva transacción
    fun addTransaction(amount: Double, date: Long, description: String?, categoryId: Long, type: TransactionType) {
        viewModelScope.launch {
            val newTransaction = Transaction(
                amount = amount,
                date = date,
                description = description,
                categoryId = categoryId,
                transactionType = type
            )
            transactionRepository.insertTransaction(newTransaction)
            // La lista se actualizará automáticamente gracias al Flow que estamos recolectando en loadInitialData()
        }
    }

    // (Opcional) Función para pre-cargar categorías si tuvieras categorías por defecto
    fun seedInitialCategoriesIfNeeded() {
        viewModelScope.launch {
            if (categoryRepository.getAllCategories().firstOrNull().isNullOrEmpty()) {
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
    }

    // Inicialmente, puedes llamar a esto desde el init del ViewModel o desde la creación de la AppDatabase.
    // Para una mejor gestión, la inicialización de datos base se suele hacer al crear la base de datos
    // usando RoomDatabase.Callback() y el método onCreate(). Por ahora, lo ponemos aquí como ejemplo.
    init {
        seedInitialCategoriesIfNeeded() // Carga categorías si la tabla está vacía
        loadInitialData()
    }
}