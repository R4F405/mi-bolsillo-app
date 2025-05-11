package com.rafa.mi_bolsillo_app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafa.mi_bolsillo_app.data.local.dao.ExpenseByCategory
import com.rafa.mi_bolsillo_app.data.local.entity.Category // <-- AÑADE IMPORT
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository // <-- AÑADE IMPORT
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
import com.rafa.mi_bolsillo_app.ui.model.TransactionUiItem // <-- IMPORTA TransactionUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first // <-- AÑADE IMPORT
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

// Actualiza DashboardUiState para usar TransactionUiItem
data class DashboardUiState(
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH), // 0-11
    val monthName: String = "", // Para mostrar el nombre del mes
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val balance: Double = 0.0,
    val recentTransactions: List<TransactionUiItem> = emptyList(), // CAMBIADO a TransactionUiItem
    val expensesByCategory: List<ExpenseByCategory> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository // <-- INYECTA CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) // 0-11

    init {
        loadDashboardData()
    }

    // (Función getCurrentMonthDateRange sin cambios)
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

            // Necesitamos todas las categorías para mapear las transacciones recientes
            val allCategories = categoryRepository.getAllCategories().first() // Obtiene la lista actual de categorías
            val categoriesMap = allCategories.associateBy { it.id }

            combine(
                transactionRepository.getTotalIncomeBetweenDates(startDate, endDate).map { it ?: 0.0 },
                transactionRepository.getTotalExpensesBetweenDates(startDate, endDate).map { it ?: 0.0 },
                transactionRepository.getTransactionsBetweenDates(startDate, endDate)
                    .map { transactions ->
                        transactions.take(5).map { transaction -> // Mapea a TransactionUiItem
                            val category = categoriesMap[transaction.categoryId]
                            TransactionUiItem(
                                id = transaction.id,
                                amount = transaction.amount,
                                date = transaction.date,
                                concepto = transaction.description, // Mapea description a concepto
                                categoryName = category?.name ?: "Sin Categoría",
                                categoryColorHex = category?.colorHex ?: "#808080",
                                transactionType = transaction.transactionType
                            )
                        }
                    },
                transactionRepository.getExpensesByCategoryInRange(startDate, endDate)
            ) { income, expenses, recents, expensesByCategoryData ->
                val calculatedBalance = income - expenses
                _uiState.value = DashboardUiState(
                    selectedYear = currentYear,
                    selectedMonth = currentMonth,
                    monthName = monthName,
                    totalIncome = income,
                    totalExpenses = expenses,
                    balance = calculatedBalance,
                    recentTransactions = recents,
                    expensesByCategory = expensesByCategoryData
                )
            }.collect {}
        }
    }
    // TODO: Implementar selectPreviousMonth, selectNextMonth
}