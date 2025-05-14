package com.rafa.mi_bolsillo_app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafa.mi_bolsillo_app.data.local.dao.ExpenseByCategory
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
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

/**
 * Modelo de vista para la pantalla de Dashboard.
 *
 * Contiene la lógica para mostrar el balance actual, ingresos y gastos, un gráfico de gastos y una lista de movimientos recientes.
 */

// Actualiza DashboardUiState para usar TransactionUiItem
data class DashboardUiState(
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH), // 0-11
    val monthName: String = "", // Para mostrar el nombre del mes
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val balance: Double = 0.0,
    val recentTransactions: List<TransactionUiItem> = emptyList(),
    val expensesByCategory: List<ExpenseByCategory> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) // 0-11

    init {
        loadDashboardData()
    }

    // Función para obtener el rango de fechas para el mes actual
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

    // Función para obtener el nombre del mes en español con la primera letra en mayúscula
    private fun getMonthYearString(year: Int, month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault()) // Cambiado a MMMM yyyy
        return sdf.format(calendar.time).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }


    // Función para cargar los datos del dashboard
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
                transactionRepository.getExpensesByCategoryInRange(startDate, endDate)
            ) { income, expenses, recents, expensesByCategoryData ->
                val calculatedBalance = income - expenses
                _uiState.value = _uiState.value.copy(
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

    // Funciones para cambiar el mes/año
    fun selectNextMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)
        calendar.add(Calendar.MONTH, 1)
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH)
        loadDashboardData() // Recarga los datos para el nuevo mes/año
    }

    // Funciones para cambiar el mes/año
    fun selectPreviousMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)
        calendar.add(Calendar.MONTH, -1)
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH)
        loadDashboardData() // Recarga los datos para el nuevo mes/año
    }
}