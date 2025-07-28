package com.rafa.mi_bolsillo_app.ui.budget

import com.rafa.mi_bolsillo_app.data.local.dao.ExpenseByCategory
import com.rafa.mi_bolsillo_app.data.local.entity.Budget
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.data.repository.BudgetRepository
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import com.rafa.mi_bolsillo_app.data.repository.SettingsRepository
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
import com.rafa.mi_bolsillo_app.ui.dashboard.FakeSettingsRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

/**
 * Tests unitarios para el BudgetViewModel.
 *
 * Se verifica la lógica de creación, actualización y eliminación de presupuestos,
 * así como el cálculo del gasto y la correcta presentación de los datos.
 */
@ExperimentalCoroutinesApi
class BudgetViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // Fakes para las dependencias
    private lateinit var fakeBudgetRepository: FakeBudgetRepository
    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository

    private lateinit var viewModel: BudgetViewModel

    // Datos de prueba
    private val categoryComida = Category(id = 1, name = "Comida", colorHex = "#FFC107")
    private val categoryOcio = Category(id = 2, name = "Ocio", colorHex = "#4CAF50")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeBudgetRepository = FakeBudgetRepository()
        fakeTransactionRepository = FakeTransactionRepository()
        fakeCategoryRepository = FakeCategoryRepository()
        fakeSettingsRepository = FakeSettingsRepository()

        fakeCategoryRepository.addCategories(listOf(categoryComida, categoryOcio))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = BudgetViewModel(
            fakeBudgetRepository,
            fakeTransactionRepository,
            fakeCategoryRepository,
            fakeSettingsRepository
        )
    }

    @Test
    fun `al iniciar - carga los presupuestos y calcula el gasto correctamente`() = runTest {
        // Preparación
        val (year, month) = getCurrentYearMonth()
        val budgetComida = Budget(id = 1, categoryId = 1, amount = 300.0, month = month + 1, year = year)
        fakeBudgetRepository.addBudget(budgetComida)

        val gastoComida = Transaction(id = 1, amount = 75.5, date = getDate(year, month, 10), description = "Gasto comida", categoryId = 1, transactionType = TransactionType.EXPENSE)
        fakeTransactionRepository.addTransaction(gastoComida)

        // Acción
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.budgetItems.size)
        val budgetUiItem = uiState.budgetItems.first()
        assertEquals(1L, budgetUiItem.budget.id)
        assertEquals(300.0, budgetUiItem.budget.amount, 0.01)
        assertEquals(75.5, budgetUiItem.spentAmount, 0.01)
        assertEquals("Comida", budgetUiItem.category.name)
    }

    @Test
    fun `upsertBudget - cuando el presupuesto no existe - lo crea correctamente`() = runTest {
        // Preparación
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Acción
        viewModel.upsertBudget(categoryId = 2, amount = 100.0)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val budgets = fakeBudgetRepository.budgetsFlow.value
        assertTrue(budgets.any { it.categoryId == 2L && it.amount == 100.0 })
        assertEquals("Presupuesto creado.", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `upsertBudget - cuando el presupuesto ya existe - lo actualiza`() = runTest {
        // Preparación
        val (year, month) = getCurrentYearMonth()
        val budgetOcio = Budget(id = 1, categoryId = 2, amount = 50.0, month = month + 1, year = year)
        fakeBudgetRepository.addBudget(budgetOcio)

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Acción
        viewModel.upsertBudget(categoryId = 2, amount = 75.0)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val updatedBudget = fakeBudgetRepository.budgetsFlow.value.find { it.id == 1L }
        assertNotNull(updatedBudget)
        assertEquals(75.0, updatedBudget!!.amount, 0.01)
        assertEquals("Presupuesto actualizado.", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `deleteBudget - elimina el presupuesto del repositorio`() = runTest {
        // Preparación
        val (year, month) = getCurrentYearMonth()
        val budgetToDelete = Budget(id = 1, categoryId = 1, amount = 100.0, month = month + 1, year = year)
        fakeBudgetRepository.addBudget(budgetToDelete)

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación inicial
        assertEquals(1, fakeBudgetRepository.budgetsFlow.value.size)

        // Acción
        viewModel.deleteBudget(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación final
        assertTrue(fakeBudgetRepository.budgetsFlow.value.isEmpty())
        assertEquals("Presupuesto eliminado.", viewModel.uiState.value.userMessage)
    }

    private fun getCurrentYearMonth(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        return Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
    }

    private fun getDate(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance().apply { set(year, month, day) }.timeInMillis
    }
}

// --- FAKE REPOSITORIES (Puedes moverlos a un archivo separado si los reutilizas mucho) ---

class FakeBudgetRepository : BudgetRepository {
    val budgetsFlow = MutableStateFlow<List<Budget>>(emptyList())

    fun addBudget(budget: Budget) {
        budgetsFlow.value += budget
    }

    override fun getBudgetsForMonth(year: Int, month: Int): Flow<List<Budget>> = flowOf(
        budgetsFlow.value.filter { it.year == year && it.month == month }
    )

    override suspend fun getBudgetForCategory(year: Int, month: Int, categoryId: Long): Budget? {
        return budgetsFlow.value.find { it.year == year && it.month == month && it.categoryId == categoryId }
    }

    override suspend fun insertBudget(budget: Budget) {
        budgetsFlow.value += budget.copy(id = (budgetsFlow.value.maxOfOrNull { it.id } ?: 0L) + 1)
    }

    override suspend fun updateBudget(budget: Budget) {
        val currentList = budgetsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == budget.id }
        if (index != -1) {
            currentList[index] = budget
            budgetsFlow.value = currentList
        }
    }

    override suspend fun deleteBudget(budget: Budget) {
        budgetsFlow.value = budgetsFlow.value.filterNot { it.id == budget.id }
    }

    override fun getFavoriteBudgets(): Flow<List<Budget>> = flowOf(budgetsFlow.value.filter { it.isFavorite })

    override suspend fun updateFavoriteStatus(budgetId: Long, isFavorite: Boolean) {
        val currentList = budgetsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == budgetId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isFavorite = isFavorite)
            budgetsFlow.value = currentList
        }
    }
}

class FakeTransactionRepository : TransactionRepository {
    private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())

    fun addTransaction(transaction: Transaction) {
        transactionsFlow.value += transaction
    }

    override fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<Transaction>> = flowOf(
        transactionsFlow.value.filter { it.date in startDate..endDate }
    )

    // Métodos no usados en este ViewModel, se pueden dejar vacíos o con valores por defecto
    override fun getAllTransactions(): Flow<List<Transaction>> = transactionsFlow
    override fun getTotalIncomeBetweenDates(startDate: Long, endDate: Long): Flow<Double?> = flowOf(0.0)
    override fun getTotalExpensesBetweenDates(startDate: Long, endDate: Long): Flow<Double?> = flowOf(0.0)
    override fun getExpensesByCategoryInRange(startDate: Long, endDate: Long): Flow<List<ExpenseByCategory>> = flowOf(emptyList())
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
