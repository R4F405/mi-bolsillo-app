package com.rafa.mi_bolsillo_app.ui.category_management

import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
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

/**
 * Tests unitarios para CategoryViewModel.
 *
 * Estos tests verifican la lógica de negocio dentro del ViewModel, como la validación
 * de entradas y las interacciones con los repositorios. Se usan implementaciones
 * "fake" de los repositorios para aislar el ViewModel.
 */
@ExperimentalCoroutinesApi
class CategoryViewModelTest {

    // Regla para manejar el Main dispatcher en los tests de coroutines
    private val testDispatcher = StandardTestDispatcher()

    // Dependencias "fake" para el ViewModel
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var fakeTransactionRepository: FakeTransactionRepository

    // La instancia del ViewModel que vamos a testear
    private lateinit var viewModel: CategoryViewModel

    @Before
    fun setUp() {
        // Establece el dispatcher principal para los tests
        Dispatchers.setMain(testDispatcher)

        // Inicializa los repositorios falsos antes de cada test
        fakeCategoryRepository = FakeCategoryRepository()
        fakeTransactionRepository = FakeTransactionRepository()

        // Crea la instancia del ViewModel con las dependencias falsas
        viewModel = CategoryViewModel(fakeCategoryRepository, fakeTransactionRepository)
    }

    @After
    fun tearDown() {
        // Limpia el dispatcher principal después de cada test
        Dispatchers.resetMain()
    }

    @Test
    fun `addCategory - cuando el nombre está en blanco - debe mostrar mensaje de error`() = runTest {
        // Acción: intentar añadir una categoría con nombre vacío
        viewModel.addCategory(" ", "#FFFFFF")

        // Avanza el dispatcher para que la coroutine se complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación: el estado de la UI debe contener el mensaje de error esperado
        val errorMessage = viewModel.uiState.value.userMessage
        assertNotNull(errorMessage)
        assertEquals("El nombre de la categoría no puede estar vacío.", errorMessage)
    }

    @Test
    fun `addCategory - cuando el nombre ya existe - debe mostrar mensaje de error`() = runTest {
        // Preparación: añadir una categoría inicial al repositorio falso
        val initialCategory = Category(id = 1, name = "Comida", colorHex = "#FFC107")
        fakeCategoryRepository.addCategory(initialCategory)

        // Carga las categorías en el ViewModel para que conozca la existente
        viewModel.loadCategoriesForTest()
        testDispatcher.scheduler.advanceUntilIdle()


        // Acción: intentar añadir una categoría con el mismo nombre (distintas mayúsculas)
        viewModel.addCategory("comida", "#FFFFFF")
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación: el estado debe contener el mensaje de error de duplicado
        val errorMessage = viewModel.uiState.value.userMessage
        assertNotNull(errorMessage)
        assertEquals("Ya existe una categoría con el nombre 'comida'.", errorMessage)
    }

    @Test
    fun `addCategory - con nombre válido - debe añadir la categoría al repositorio`() = runTest {
        // Acción: añadir una categoría válida
        viewModel.addCategory("Viajes", "#2196F3")
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación: la categoría debe existir en el repositorio falso
        val categories = fakeCategoryRepository.categoriesFlow.value
        assertTrue(categories.any { it.name == "Viajes" && it.colorHex == "#2196F3" })
        assertEquals("Categoría 'Viajes' añadida.", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `deleteCategory - con transacciones asociadas - debe mostrar mensaje de error`() = runTest {
        // Preparación: añadir una categoría y una transacción asociada a ella
        val categoryToDelete = Category(id = 1, name = "Ocio", colorHex = "#4CAF50")
        fakeCategoryRepository.addCategory(categoryToDelete)
        fakeTransactionRepository.addTransaction(
            Transaction(id = 1, amount = 10.0, date = 0L, description = "Cine", categoryId = 1, transactionType = TransactionType.EXPENSE)
        )
        viewModel.loadCategoriesForTest()
        testDispatcher.scheduler.advanceUntilIdle()

        // Acción: intentar eliminar la categoría
        viewModel.deleteCategory(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación: el estado debe mostrar un mensaje de error y la categoría no debe ser eliminada
        val errorMessage = viewModel.uiState.value.userMessage
        assertNotNull(errorMessage)
        assertTrue(errorMessage!!.contains("No se puede eliminar 'Ocio', tiene 1 transacciones asociadas."))
        assertTrue(fakeCategoryRepository.categoriesFlow.value.any { it.id == 1L })
    }

    @Test
    fun `deleteCategory - sin transacciones asociadas - debe eliminar la categoría`() = runTest {
        // Preparación: añadir una categoría sin transacciones
        val categoryToDelete = Category(id = 1, name = "Regalos", colorHex = "#E91E63")
        fakeCategoryRepository.addCategory(categoryToDelete)
        viewModel.loadCategoriesForTest()
        testDispatcher.scheduler.advanceUntilIdle()

        // Acción: eliminar la categoría
        viewModel.deleteCategory(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación: la categoría ya no debe existir en el repositorio
        val categories = fakeCategoryRepository.categoriesFlow.value
        assertTrue(categories.none { it.id == 1L })
        assertEquals("Categoría 'Regalos' eliminada.", viewModel.uiState.value.userMessage)
    }
}

/**
 * Implementación falsa de CategoryRepository para usar en tests.
 * Simula el comportamiento de la base de datos en memoria.
 */
class FakeCategoryRepository : CategoryRepository {
    val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())
    private var nextId = 1L

    suspend fun addCategory(category: Category) {
        val currentList = categoriesFlow.value.toMutableList()
        currentList.add(category.copy(id = category.id.takeIf { it != 0L } ?: nextId++))
        categoriesFlow.value = currentList
    }

    override fun getAllCategories(): Flow<List<Category>> = categoriesFlow
    override fun getUserDefinedCategories(): Flow<List<Category>> = flowOf(categoriesFlow.value.filter { !it.isPredefined })
    override fun getPredefinedCategories(): Flow<List<Category>> = flowOf(categoriesFlow.value.filter { it.isPredefined })
    override suspend fun getCategoryById(id: Long): Category? = categoriesFlow.value.find { it.id == id }
    override suspend fun insertCategory(category: Category): Long {
        addCategory(category)
        return category.id
    }
    override suspend fun insertCategories(categories: List<Category>) {
        categories.forEach { addCategory(it) }
    }
    override suspend fun updateCategory(category: Category) {
        val currentList = categoriesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == category.id }
        if (index != -1) {
            currentList[index] = category
            categoriesFlow.value = currentList
        }
    }
    override suspend fun deleteCategory(category: Category) {
        val currentList = categoriesFlow.value.toMutableList()
        currentList.removeAll { it.id == category.id }
        categoriesFlow.value = currentList
    }
}

/**
 * Implementación falsa de TransactionRepository para usar en tests.
 */
class FakeTransactionRepository : TransactionRepository {
    private val transactions = mutableListOf<Transaction>()

    suspend fun addTransaction(transaction: Transaction) {
        transactions.add(transaction)
    }

    override fun getTransactionsByCategoryId(categoryId: Long): Flow<List<Transaction>> {
        return flowOf(transactions.filter { it.categoryId == categoryId })
    }

    // Implementaciones no necesarias para este test, se dejan vacías o con valores por defecto
    override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
    override fun getTransactionsByType(transactionType: TransactionType): Flow<List<Transaction>> = flowOf(emptyList())
    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> = flowOf(emptyList())
    override suspend fun getTransactionById(id: Long): Transaction? = null
    override suspend fun insertTransaction(transaction: Transaction) {}
    override suspend fun updateTransaction(transaction: Transaction) {}
    override suspend fun deleteTransaction(transaction: Transaction) {}
    override fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<Transaction>> = flowOf(emptyList())
    override fun getTotalIncomeBetweenDates(startDate: Long, endDate: Long): Flow<Double?> = flowOf(0.0)
    override fun getTotalExpensesBetweenDates(startDate: Long, endDate: Long): Flow<Double?> = flowOf(0.0)
    override fun getExpensesByCategoryInRange(startDate: Long, endDate: Long): Flow<List<com.rafa.mi_bolsillo_app.data.local.dao.ExpenseByCategory>> = flowOf(emptyList())
}

// Función de extensión para facilitar la carga de datos en el ViewModel durante los tests
fun CategoryViewModel.loadCategoriesForTest() {
}
