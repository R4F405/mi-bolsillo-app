package com.rafa.mi_bolsillo_app.ui.dashboard

import com.rafa.mi_bolsillo_app.data.local.dao.ExpenseByCategory
import com.rafa.mi_bolsillo_app.data.local.entity.Budget
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.data.repository.BudgetRepository
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import com.rafa.mi_bolsillo_app.data.repository.SettingsRepository
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
import com.rafa.mi_bolsillo_app.ui.settings.theme.ThemeOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Currency

/**
 * Tests unitarios para el DashboardViewModel.
 *
 * Se verifica la lógica de cálculo de balance, la navegación entre meses y la correcta
 * presentación de los datos combinados de múltiples repositorios.
 */
@ExperimentalCoroutinesApi
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // Fakes para todas las dependencias del ViewModel
    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var fakeBudgetRepository: FakeBudgetRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository

    private lateinit var viewModel: DashboardViewModel

    // Datos de prueba
    private val categorySalario = Category(id = 1, name = "Salario", colorHex = "#009688")
    private val categoryComida = Category(id = 2, name = "Comida", colorHex = "#FFC107")
    private val categoryTransporte = Category(id = 3, name = "Transporte", colorHex = "#2196F3")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeTransactionRepository = FakeTransactionRepository()
        fakeCategoryRepository = FakeCategoryRepository()
        fakeBudgetRepository = FakeBudgetRepository()
        fakeSettingsRepository = FakeSettingsRepository()

        // Poblar repositorios con datos iniciales
        fakeCategoryRepository.addCategories(listOf(categorySalario, categoryComida, categoryTransporte))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Función para crear el ViewModel después de configurar los datos de prueba
    private fun createViewModel() {
        viewModel = DashboardViewModel(
            fakeTransactionRepository,
            fakeCategoryRepository,
            fakeBudgetRepository,
            fakeSettingsRepository
        )
    }

    @Test
    fun `al iniciar - calcula el balance y los totales correctamente para el mes actual`() = runTest {
        // Preparación: Añadir transacciones para el mes actual
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        fakeTransactionRepository.addTransactions(listOf(
            Transaction(id = 1, amount = 2000.0, date = getDate(year, month, 1), description = "Salario", categoryId = 1, transactionType = TransactionType.INCOME),
            Transaction(id = 2, amount = 50.0, date = getDate(year, month, 5), description = "Supermercado", categoryId = 2, transactionType = TransactionType.EXPENSE),
            Transaction(id = 3, amount = 25.0, date = getDate(year, month, 10), description = "Gasolina", categoryId = 3, transactionType = TransactionType.EXPENSE)
        ))

        // Acción: Crear el ViewModel (esto dispara la carga de datos en su `init`)
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle() // Asegura que todas las coroutines se completen

        // Verificación
        val uiState = viewModel.uiState.value
        assertEquals(2000.0, uiState.totalIncome, 0.01)
        assertEquals(75.0, uiState.totalExpenses, 0.01)
        assertEquals(1925.0, uiState.balance, 0.01)
        assertEquals(3, uiState.recentTransactions.size)
    }

    @Test
    fun `selectNextMonth - actualiza los datos al mes siguiente`() = runTest {
        // Preparación: Transacciones en el mes actual y en el siguiente
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        // Transacción para el mes actual
        fakeTransactionRepository.addTransaction(Transaction(id = 1, amount = 100.0, date = getDate(currentYear, currentMonth, 15), description = "Actual", categoryId = 2, transactionType = TransactionType.EXPENSE))

        // Transacción para el mes siguiente
        val nextMonthCalendar = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
        val nextYear = nextMonthCalendar.get(Calendar.YEAR)
        val nextMonth = nextMonthCalendar.get(Calendar.MONTH)
        fakeTransactionRepository.addTransaction(Transaction(id = 2, amount = 500.0, date = getDate(nextYear, nextMonth, 5), description = "Siguiente", categoryId = 2, transactionType = TransactionType.EXPENSE))

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación inicial (mes actual)
        assertEquals(100.0, viewModel.uiState.value.totalExpenses, 0.01)

        // Acción: Mover al mes siguiente
        viewModel.selectNextMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación final (mes siguiente)
        val uiState = viewModel.uiState.value
        assertEquals(0.0, uiState.totalIncome, 0.01)
        assertEquals(500.0, uiState.totalExpenses, 0.01)
        assertEquals(-500.0, uiState.balance, 0.01)
        assertEquals(1, uiState.recentTransactions.size)
        assertEquals("Siguiente", uiState.recentTransactions.first().concepto)
    }

    @Test
    fun `loadDashboardData - filtra y muestra solo los presupuestos favoritos del mes seleccionado`() = runTest {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) // 0-11

        // Preparación:
        // Presupuesto favorito para el mes actual (mes en Calendar es 0-11, pero en Budget es 1-12)
        fakeBudgetRepository.addBudget(Budget(id = 1, categoryId = 2, amount = 200.0, month = month + 1, year = year, isFavorite = true))
        // Presupuesto favorito para OTRO mes
        fakeBudgetRepository.addBudget(Budget(id = 2, categoryId = 3, amount = 100.0, month = month, year = year, isFavorite = true)) // Mes anterior
        // Presupuesto NO favorito para el mes actual
        fakeBudgetRepository.addBudget(Budget(id = 3, categoryId = 3, amount = 50.0, month = month + 1, year = year, isFavorite = false))

        // Acción
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.favoriteBudgets.size)
        assertEquals(1L, uiState.favoriteBudgets.first().budget.id)
        assertEquals(2L, uiState.favoriteBudgets.first().category.id) // Categoria "Comida"
    }

    // Helper para crear timestamps para fechas específicas
    private fun getDate(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month, day)
        }.timeInMillis
    }
}


// --- FAKE REPOSITORIES ---

class FakeTransactionRepository : TransactionRepository {
    private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())

    fun addTransaction(transaction: Transaction) {
        transactionsFlow.value += transaction
    }
    fun addTransactions(transactions: List<Transaction>) {
        transactionsFlow.value += transactions
    }

    override fun getAllTransactions(): Flow<List<Transaction>> = transactionsFlow
    override fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<Transaction>> = flowOf(
        transactionsFlow.value.filter { it.date in startDate..endDate }
    )
    override fun getTotalIncomeBetweenDates(startDate: Long, endDate: Long): Flow<Double?> = flowOf(
        transactionsFlow.value
            .filter { it.transactionType == TransactionType.INCOME && it.date in startDate..endDate }
            .sumOf { it.amount }
    )
    override fun getTotalExpensesBetweenDates(startDate: Long, endDate: Long): Flow<Double?> = flowOf(
        transactionsFlow.value
            .filter { it.transactionType == TransactionType.EXPENSE && it.date in startDate..endDate }
            .sumOf { it.amount }
    )
    override fun getExpensesByCategoryInRange(startDate: Long, endDate: Long): Flow<List<ExpenseByCategory>> = flowOf(
        transactionsFlow.value
            .filter { it.transactionType == TransactionType.EXPENSE && it.date in startDate..endDate }
            .groupBy { it.categoryId }
            .map { (categoryId, trans) ->
                ExpenseByCategory(
                    categoryName = "Category $categoryId",
                    categoryColorHex = "#FFFFFF",
                    totalAmount = trans.sumOf { it.amount }
                )
            }
    )
    // Métodos no usados en este ViewModel, se pueden dejar vacíos
    override fun getTransactionsByType(transactionType: TransactionType): Flow<List<Transaction>> = flowOf(emptyList())
    override fun getTransactionsByCategoryId(categoryId: Long): Flow<List<Transaction>> = flowOf(emptyList())
    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> = flowOf(emptyList())
    override suspend fun getTransactionById(id: Long): Transaction? = null
    override suspend fun insertTransaction(transaction: Transaction) {}
    override suspend fun updateTransaction(transaction: Transaction) {}
    override suspend fun deleteTransaction(transaction: Transaction) {}
}

class FakeCategoryRepository : CategoryRepository {
    private val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())
    fun addCategories(categories: List<Category>) {
        categoriesFlow.value += categories
    }
    override fun getAllCategories(): Flow<List<Category>> = categoriesFlow
    // Métodos no usados
    override fun getUserDefinedCategories(): Flow<List<Category>> = flowOf(emptyList())
    override fun getPredefinedCategories(): Flow<List<Category>> = flowOf(emptyList())
    override suspend fun getCategoryById(id: Long): Category? = null
    override suspend fun insertCategory(category: Category): Long = 0
    override suspend fun insertCategories(categories: List<Category>) {}
    override suspend fun updateCategory(category: Category) {}
    override suspend fun deleteCategory(category: Category) {}
}

class FakeBudgetRepository : BudgetRepository {
    private val budgetsFlow = MutableStateFlow<List<Budget>>(emptyList())
    fun addBudget(budget: Budget) {
        budgetsFlow.value += budget
    }
    override fun getFavoriteBudgets(): Flow<List<Budget>> = flowOf(budgetsFlow.value.filter { it.isFavorite })
    // Métodos no usados
    override fun getBudgetsForMonth(year: Int, month: Int): Flow<List<Budget>> = flowOf(emptyList())
    override suspend fun getBudgetForCategory(year: Int, month: Int, categoryId: Long): Budget? = null
    override suspend fun insertBudget(budget: Budget) {}
    override suspend fun updateBudget(budget: Budget) {}
    override suspend fun deleteBudget(budget: Budget) {}
    override suspend fun updateFavoriteStatus(budgetId: Long, isFavorite: Boolean) {}
}

class FakeSettingsRepository : SettingsRepository {
    override val currency: Flow<Currency> = flowOf(Currency.getInstance("EUR"))
    override val theme: Flow<ThemeOption> = flowOf(ThemeOption.SYSTEM)
    override val appLockEnabled: Flow<Boolean> = flowOf(false)
    override suspend fun saveCurrency(currencyCode: String) {}
    override suspend fun saveTheme(theme: ThemeOption) {}
    override suspend fun setAppLockEnabled(isEnabled: Boolean) {}
}
