package com.rafa.mi_bolsillo_app.ui.budget

import com.rafa.mi_bolsillo_app.data.local.entity.Budget
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.fakes.FakeBudgetRepository
import com.rafa.mi_bolsillo_app.fakes.FakeCategoryRepository
import com.rafa.mi_bolsillo_app.fakes.FakeSettingsRepository
import com.rafa.mi_bolsillo_app.fakes.FakeTransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

        // Metodo del fake para poblar los datos
        runTest {
            fakeCategoryRepository.insertCategories(listOf(categoryComida, categoryOcio))
        }
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
        fakeBudgetRepository.insertBudget(budgetComida)

        val gastoComida = Transaction(id = 1, amount = 75.5, date = getDate(year, month, 10), description = "Gasto comida", categoryId = 1, transactionType = TransactionType.EXPENSE)
        fakeTransactionRepository.insertTransaction(gastoComida)

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
    fun `upsertBudget - con monto cero o negativo - debe mostrar mensaje de error`() = runTest {
        // Preparación
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val initialBudgetCount = fakeBudgetRepository.budgetsFlow.value.size

        // Acción
        viewModel.upsertBudget(categoryId = 1, amount = -50.0)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        assertEquals("El monto debe ser positivo.", viewModel.uiState.value.userMessage)
        assertEquals(initialBudgetCount, fakeBudgetRepository.budgetsFlow.value.size) // No se debe añadir
    }

    @Test
    fun `upsertBudget - cuando el presupuesto ya existe - lo actualiza`() = runTest {
        // Preparación
        val (year, month) = getCurrentYearMonth()
        val budgetOcio = Budget(id = 1, categoryId = 2, amount = 50.0, month = month + 1, year = year)
        fakeBudgetRepository.insertBudget(budgetOcio)

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
        fakeBudgetRepository.insertBudget(budgetToDelete)

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