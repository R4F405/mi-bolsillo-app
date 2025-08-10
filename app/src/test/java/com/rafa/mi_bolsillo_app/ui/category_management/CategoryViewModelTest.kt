package com.rafa.mi_bolsillo_app.ui.category_management

import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.fakes.FakeCategoryRepository
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

/**
 * Tests unitarios para CategoryViewModel.
 *
 * Estos tests verifican la lógica de negocio dentro del ViewModel, como la validación
 * de entradas y las interacciones con los repositorios. Se usan implementaciones
 * "fake" de los repositorios para aislar el ViewModel.
 */
@ExperimentalCoroutinesApi
class CategoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var viewModel: CategoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCategoryRepository = FakeCategoryRepository()
        fakeTransactionRepository = FakeTransactionRepository()
        viewModel = CategoryViewModel(fakeCategoryRepository, fakeTransactionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addCategory - cuando el nombre está en blanco - debe mostrar mensaje de error`() = runTest {
        viewModel.addCategory(" ", "#FFFFFF")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("El nombre de la categoría no puede estar vacío.", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `addCategory - cuando el nombre ya existe - debe mostrar mensaje de error`() = runTest {
        val initialCategory = Category(id = 1, name = "Comida", colorHex = "#FFC107")
        fakeCategoryRepository.insertCategory(initialCategory)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addCategory("comida", "#FFFFFF")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Ya existe una categoría con el nombre 'comida'.", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `addCategory - con nombre válido - debe añadir la categoría al repositorio`() = runTest {
        viewModel.addCategory("Viajes", "#2196F3")
        testDispatcher.scheduler.advanceUntilIdle()

        val categories = fakeCategoryRepository.categoriesFlow.value
        assertTrue(categories.any { it.name == "Viajes" && it.colorHex == "#2196F3" })
        assertEquals("Categoría 'Viajes' añadida.", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `updateCategory - cuando el nuevo nombre ya existe en otra categoria - debe mostrar mensaje de error`() = runTest {
        val categoria1 = Category(id = 1, name = "Ocio", colorHex = "#FFC107")
        val categoria2 = Category(id = 2, name = "Comida", colorHex = "#4CAF50")
        fakeCategoryRepository.insertCategories(listOf(categoria1, categoria2))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateCategory(id = 1, name = "Comida", colorHex = "#FFFFFF")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Ya existe otra categoría con el nombre 'Comida'.", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `deleteCategory - con transacciones asociadas - debe mostrar mensaje de error`() = runTest {
        val categoryToDelete = Category(id = 1, name = "Ocio", colorHex = "#4CAF50")
        fakeCategoryRepository.insertCategory(categoryToDelete)
        fakeTransactionRepository.insertTransaction(
            Transaction(id = 1, amount = 10.0, date = 0L, description = "Cine", categoryId = 1, transactionType = TransactionType.EXPENSE)
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteCategory(1)
        testDispatcher.scheduler.advanceUntilIdle()

        val errorMessage = viewModel.uiState.value.userMessage
        assertNotNull(errorMessage)
        assertTrue(errorMessage!!.contains("No se puede eliminar 'Ocio', tiene 1 transacciones asociadas."))
        assertTrue(fakeCategoryRepository.categoriesFlow.value.any { it.id == 1L })
    }

    @Test
    fun `deleteCategory - sin transacciones asociadas - debe eliminar la categoría`() = runTest {
        val categoryToDelete = Category(id = 1, name = "Regalos", colorHex = "#E91E63")
        fakeCategoryRepository.insertCategory(categoryToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteCategory(1)
        testDispatcher.scheduler.advanceUntilIdle()

        val categories = fakeCategoryRepository.categoriesFlow.value
        assertTrue(categories.none { it.id == 1L })
        assertEquals("Categoría 'Regalos' eliminada.", viewModel.uiState.value.userMessage)
    }
}