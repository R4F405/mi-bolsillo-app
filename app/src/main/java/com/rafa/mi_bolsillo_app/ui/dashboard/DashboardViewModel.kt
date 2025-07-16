package com.rafa.mi_bolsillo_app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafa.mi_bolsillo_app.data.local.dao.ExpenseByCategory
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.data.repository.BudgetRepository
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
import com.rafa.mi_bolsillo_app.ui.budget.BudgetUiItem
import com.rafa.mi_bolsillo_app.ui.model.TransactionUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class DashboardUiState(
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH), // 0-11
    val monthName: String = "",
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val balance: Double = 0.0,
    val recentTransactions: List<TransactionUiItem> = emptyList(),
    val expensesByCategory: List<ExpenseByCategory> = emptyList(),
    val favoriteBudgets: List<BudgetUiItem> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) // 0-11

    init {
        loadDashboardData()
    }

    private fun getCurrentMonthDateRange(year: Int, month: Int): Pair<Long, Long> {
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


    fun loadDashboardData() {
        viewModelScope.launch {
            val (startDate, endDate) = getCurrentMonthDateRange(currentYear, currentMonth)
            val monthName = getMonthYearString(currentYear, currentMonth)

            val allCategories = categoryRepository.getAllCategories().first()
            val categoriesMap = allCategories.associateBy { it.id }

            combine(
                transactionRepository.getTotalIncomeBetweenDates(startDate, endDate).map { it ?: 0.0 },
                transactionRepository.getTotalExpensesBetweenDates(startDate, endDate).map { it ?: 0.0 },
                transactionRepository.getTransactionsBetweenDates(startDate, endDate)
                    .map { transactions ->
                        transactions.take(3).map { transaction ->
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
                    },
                transactionRepository.getExpensesByCategoryInRange(startDate, endDate),
                budgetRepository.getFavoriteBudgets()
            ) { income, expenses, recents, expensesByCategoryData, favoriteBudgets ->

                val favoriteBudgetUiItems = favoriteBudgets
                    .filter { budget ->
                        // Filtra para que solo se incluyan los del mes y año actual
                        budget.year == currentYear && budget.month == (currentMonth + 1)
                    }
                    .map { budget ->
                        val cat = allCategories.find { it.id == budget.categoryId }
                        // El gasto sí se calcula sobre el rango de fechas actual, esto estaba bien
                        val spent = transactionRepository.getTransactionsBetweenDates(startDate, endDate)
                            .first()
                            .filter { it.categoryId == budget.categoryId && it.transactionType == TransactionType.EXPENSE }
                            .sumOf { it.amount }

                        BudgetUiItem(
                            budget = budget,
                            category = cat ?: Category(0, "Desconocida", "", "", false),
                            spentAmount = spent
                        )
                    }

                val calculatedBalance = income - expenses
                _uiState.value = _uiState.value.copy(
                    selectedYear = currentYear,
                    selectedMonth = currentMonth,
                    monthName = monthName,
                    totalIncome = income,
                    totalExpenses = expenses,
                    balance = calculatedBalance,
                    recentTransactions = recents,
                    expensesByCategory = expensesByCategoryData,
                    favoriteBudgets = favoriteBudgetUiItems
                )
            }.collect {}
        }
    }

    fun selectNextMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)
        calendar.add(Calendar.MONTH, 1)
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH)
        loadDashboardData()
    }

    fun selectPreviousMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)
        calendar.add(Calendar.MONTH, -1)
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH)
        loadDashboardData()
    }
}