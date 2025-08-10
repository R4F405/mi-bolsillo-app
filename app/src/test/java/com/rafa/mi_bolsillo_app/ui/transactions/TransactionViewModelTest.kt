package com.rafa.mi_bolsillo_app.ui.transactions

import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class TransactionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: TransactionViewModel
    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeTransactionRepository = FakeTransactionRepository()
        fakeCategoryRepository = FakeCategoryRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        viewModel = TransactionViewModel(
            fakeTransactionRepository,
            fakeCategoryRepository,
            fakeSettingsRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addTransaction con datos validos guarda la transaccion correctamente`() = runTest {
        // Preparación
        val category = Category(id = 1, name = "Test", colorHex = "#FFFFFF")
        fakeCategoryRepository.insertCategory(category)
        val initialCount = fakeTransactionRepository.transactionsFlow.value.size

        // Acción
        viewModel.addTransaction(
            amount = 50.0,
            date = System.currentTimeMillis(),
            concepto = "Compra de prueba",
            categoryId = 1,
            type = TransactionType.EXPENSE
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val newTransactions = fakeTransactionRepository.transactionsFlow.value
        assertEquals(initialCount + 1, newTransactions.size)
        assertEquals("Compra de prueba", newTransactions.last().description)
    }

    @Test
    fun `loadTransactionForEditing con ID valido actualiza el StateFlow transactionToEdit`() = runTest {
        // Preparación
        val transaction = Transaction(id = 123, amount = 100.0, date = 1L, description = "Para editar", categoryId = 1, transactionType = TransactionType.INCOME)
        fakeTransactionRepository.insertTransaction(transaction)

        // Acción
        viewModel.loadTransactionForEditing(123L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val transactionToEdit = viewModel.transactionToEdit.value
        assertNotNull(transactionToEdit)
        assertEquals(123L, transactionToEdit?.id)
        assertEquals("Para editar", transactionToEdit?.description)
    }

    @Test
    fun `clearEditingTransaction limpia el estado de transactionToEdit`() = runTest {
        // Preparación: Carga una transacción para editar primero.
        val transaction = Transaction(id = 123, amount = 100.0, date = 1L, description = "Para editar", categoryId = 1, transactionType = TransactionType.INCOME)
        fakeTransactionRepository.insertTransaction(transaction)
        viewModel.loadTransactionForEditing(123L)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.transactionToEdit.value) // Nos aseguramos de que no es nulo

        // Acción
        viewModel.clearEditingTransaction()

        // Verificación
        assertNull(viewModel.transactionToEdit.value)
    }


    @Test
    fun `deleteTransaction con ID valido elimina la transaccion del repositorio`() = runTest {
        // Preparación
        val transaction = Transaction(id = 1, amount = 10.0, date = 1L, description = "Test", categoryId = 1, transactionType = TransactionType.EXPENSE)
        fakeTransactionRepository.insertTransaction(transaction)
        assertEquals(1, fakeTransactionRepository.transactionsFlow.value.size)

        // Acción
        viewModel.deleteTransaction(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        assertTrue(fakeTransactionRepository.transactionsFlow.value.isEmpty())
    }
}