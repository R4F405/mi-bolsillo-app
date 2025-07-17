package com.rafa.mi_bolsillo_app.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafa.mi_bolsillo_app.data.local.entity.Budget
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.repository.BudgetRepository
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import com.rafa.mi_bolsillo_app.data.repository.SettingsRepository
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.Currency
import javax.inject.Inject

// Modelo para un item de presupuesto en la UI
data class BudgetUiItem(
    val budget: Budget,
    val category: Category,
    val spentAmount: Double
)

// Estado de la pantalla de presupuestos
data class BudgetScreenUiState(
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH), // 0-11
    val monthName: String = "",
    val budgetItems: List<BudgetUiItem> = emptyList(),
    val availableCategories: List<Category> = emptyList(), // Categorías sin presupuesto este mes
    val isLoading: Boolean = true,
    val userMessage: String? = null,
    val currency: Currency = Currency.getInstance("EUR") // Valor inicial
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository // Inyectamos el nuevo repositorio
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetScreenUiState())
    val uiState: StateFlow<BudgetScreenUiState> = _uiState.asStateFlow()

    private val selectedDateFlow = MutableStateFlow(
        Pair(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH))
    )

    init {
        viewModelScope.launch {
            selectedDateFlow.flatMapLatest { (year, month) ->
                loadDataForMonth(year, month)
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun loadDataForMonth(year: Int, month: Int): Flow<BudgetScreenUiState> {
        val (startDate, endDate) = getDateRangeForMonth(year, month)
        val monthName = getMonthYearString(year, month)

        // Flujo de todas las categorías de gastos
        val expenseCategoriesFlow = categoryRepository.getAllCategories()
            .map { list -> list.filter { it.name != "Salario" && it.name != "Otros Ingresos" } } // Filtrar categorías de ingresos

        // Flujo de los presupuestos para el mes
        val budgetsFlow = budgetRepository.getBudgetsForMonth(year, month + 1) // DAO usa 1-12, Calendar usa 0-11

        // Flujo de los gastos para el mes
        val expensesFlow = transactionRepository.getTransactionsBetweenDates(startDate, endDate)
            .map { transactions ->
                transactions
                    .filter { it.transactionType == com.rafa.mi_bolsillo_app.data.local.entity.TransactionType.EXPENSE }
                    .groupBy { it.categoryId }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }
            }

        return combine(
            expenseCategoriesFlow,
            budgetsFlow,
            expensesFlow,
            settingsRepository.currency
        ) { categories, budgets, expenses, currency ->
            val budgetCategoryIds = budgets.map { it.categoryId }.toSet()

            val budgetItems = budgets.map { budget ->
                val category = categories.find { it.id == budget.categoryId }
                BudgetUiItem(
                    budget = budget,
                    category = category ?: Category(0, "Desconocida",  "#CCCCCC", false),
                    spentAmount = expenses[budget.categoryId] ?: 0.0
                )
            }.sortedBy { it.category.name }

            val availableCategories = categories.filter { it.id !in budgetCategoryIds }

            BudgetScreenUiState(
                selectedYear = year,
                selectedMonth = month,
                monthName = monthName,
                budgetItems = budgetItems,
                availableCategories = availableCategories,
                isLoading = false,
                currency = currency // Añadimos la moneda al estado
            )
        }
    }

    fun upsertBudget(categoryId: Long, amount: Double) {
        viewModelScope.launch {
            val year = selectedDateFlow.value.first
            val month = selectedDateFlow.value.second + 1 // a 1-12

            if (amount <= 0) {
                _uiState.update { it.copy(userMessage = "El monto debe ser positivo.") }
                return@launch
            }

            val existingBudget = budgetRepository.getBudgetForCategory(year, month, categoryId)

            if (existingBudget != null) {
                budgetRepository.updateBudget(existingBudget.copy(amount = amount))
                _uiState.update { it.copy(userMessage = "Presupuesto actualizado.") }
            } else {
                val newBudget = Budget(categoryId = categoryId, amount = amount, month = month, year = year)
                budgetRepository.insertBudget(newBudget)
                _uiState.update { it.copy(userMessage = "Presupuesto creado.") }
            }
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            val itemToDelete = _uiState.value.budgetItems.find { it.budget.id == budgetId }
            itemToDelete?.let {
                budgetRepository.deleteBudget(it.budget)
                _uiState.update { state -> state.copy(userMessage = "Presupuesto eliminado.") }
            }
        }
    }

    fun toggleFavoriteStatus(budgetId: Long) {
        viewModelScope.launch {
            val budget = uiState.value.budgetItems.find { it.budget.id == budgetId }?.budget
            budget?.let {
                budgetRepository.updateFavoriteStatus(it.id, !it.isFavorite)
            }
        }
    }

    fun selectNextMonth() {
        val calendar = Calendar.getInstance().apply {
            set(selectedDateFlow.value.first, selectedDateFlow.value.second, 1)
            add(Calendar.MONTH, 1)
        }
        selectedDateFlow.value = Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
    }

    fun selectPreviousMonth() {
        val calendar = Calendar.getInstance().apply {
            set(selectedDateFlow.value.first, selectedDateFlow.value.second, 1)
            add(Calendar.MONTH, -1)
        }
        selectedDateFlow.value = Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun getDateRangeForMonth(year: Int, month: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis
        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.timeInMillis
        return Pair(startDate, endDate)
    }

    private fun getMonthYearString(year: Int, month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(calendar.time).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}