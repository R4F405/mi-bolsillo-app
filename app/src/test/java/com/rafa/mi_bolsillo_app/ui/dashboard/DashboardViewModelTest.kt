package com.rafa.mi_bolsillo_app.ui.dashboard

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
import org.junit.Before
import org.junit.Test
import java.util.Calendar

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
        runTest {
            fakeCategoryRepository.insertCategories(listOf(categorySalario, categoryComida, categoryTransporte))
        }
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

        val transactions = listOf(
            Transaction(id = 1, amount = 2000.0, date = getDate(year, month, 1), description = "Salario", categoryId = 1, transactionType = TransactionType.INCOME),
            Transaction(id = 2, amount = 50.0, date = getDate(year, month, 5), description = "Supermercado", categoryId = 2, transactionType = TransactionType.EXPENSE),
            Transaction(id = 3, amount = 25.0, date = getDate(year, month, 10), description = "Gasolina", categoryId = 3, transactionType = TransactionType.EXPENSE)
        )
        transactions.forEach { fakeTransactionRepository.insertTransaction(it) }


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
    fun `al iniciar - cuando no hay datos - los totales deben ser cero y las listas vacias`() = runTest {
        // Preparación: No se añaden transacciones.

        // Acción: Crear el ViewModel
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val uiState = viewModel.uiState.value
        assertEquals(0.0, uiState.totalIncome, 0.01)
        assertEquals(0.0, uiState.totalExpenses, 0.01)
        assertEquals(0.0, uiState.balance, 0.01)
        assertEquals(true, uiState.recentTransactions.isEmpty())
        assertEquals(true, uiState.expensesByCategory.isEmpty())
        assertEquals(true, uiState.favoriteBudgets.isEmpty())
    }

    @Test
    fun `selectNextMonth - actualiza los datos al mes siguiente`() = runTest {
        // Preparación: Transacciones en el mes actual y en el siguiente
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        // Transacción para el mes actual
        fakeTransactionRepository.insertTransaction(Transaction(id = 1, amount = 100.0, date = getDate(currentYear, currentMonth, 15), description = "Actual", categoryId = 2, transactionType = TransactionType.EXPENSE))

        // Transacción para el mes siguiente
        val nextMonthCalendar = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
        val nextYear = nextMonthCalendar.get(Calendar.YEAR)
        val nextMonth = nextMonthCalendar.get(Calendar.MONTH)
        fakeTransactionRepository.insertTransaction(Transaction(id = 2, amount = 500.0, date = getDate(nextYear, nextMonth, 5), description = "Siguiente", categoryId = 2, transactionType = TransactionType.EXPENSE))

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
        fakeBudgetRepository.insertBudget(Budget(id = 1, categoryId = 2, amount = 200.0, month = month + 1, year = year, isFavorite = true))
        // Presupuesto favorito para OTRO mes
        fakeBudgetRepository.insertBudget(Budget(id = 2, categoryId = 3, amount = 100.0, month = month, year = year, isFavorite = true)) // Mes anterior
        // Presupuesto NO favorito para el mes actual
        fakeBudgetRepository.insertBudget(Budget(id = 3, categoryId = 3, amount = 50.0, month = month + 1, year = year, isFavorite = false))

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