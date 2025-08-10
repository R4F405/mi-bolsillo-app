package com.rafa.mi_bolsillo_app.ui.recurring_transactions

import com.rafa.mi_bolsillo_app.data.local.entity.RecurrenceFrequency
import com.rafa.mi_bolsillo_app.data.local.entity.RecurringTransaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.fakes.FakeCategoryRepository
import com.rafa.mi_bolsillo_app.fakes.FakeRecurringTransactionRepository
import com.rafa.mi_bolsillo_app.fakes.FakeSettingsRepository
import com.rafa.mi_bolsillo_app.utils.RecurrenceHelper
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
import java.util.Calendar

@ExperimentalCoroutinesApi
class RecurringTransactionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: RecurringTransactionViewModel
    private lateinit var fakeRecurringTransactionRepository: FakeRecurringTransactionRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRecurringTransactionRepository = FakeRecurringTransactionRepository()
        fakeCategoryRepository = FakeCategoryRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        viewModel = RecurringTransactionViewModel(
            fakeRecurringTransactionRepository,
            fakeCategoryRepository,
            fakeSettingsRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveRecurringTransaction con nombre en blanco muestra mensaje de error`() = runTest {
        // Acción
        viewModel.saveRecurringTransaction(
            id = null,
            name = " ", // Nombre en blanco
            amount = 50.0,
            description = null,
            categoryId = 1,
            transactionType = TransactionType.EXPENSE,
            startDate = 0L,
            frequency = RecurrenceFrequency.MONTHLY,
            interval = 1,
            dayOfMonth = null,
            monthOfYear = null,
            endDate = null,
            isActive = true
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        assertEquals(
            "El nombre de la plantilla es obligatorio.",
            viewModel.uiState.value.userMessage
        )
    }

    @Test
    fun `saveRecurringTransaction nueva plantilla con fecha de inicio pasada calcula la proxima ocurrencia futura`() = runTest {
        // Preparación
        // La fecha de inicio está en el pasado lejano
        val startDate = Calendar.getInstance().apply { set(2023, Calendar.JANUARY, 15) }.timeInMillis

        // El ViewModel debe calcular la primera ocurrencia que sea posterior a HOY.
        var expectedNextDate = startDate
        while (expectedNextDate < System.currentTimeMillis()) {
            expectedNextDate = RecurrenceHelper.calculateNextOccurrenceDate(
                expectedNextDate, RecurrenceFrequency.MONTHLY, 1, 15, null
            )
        }

        // Acción
        viewModel.saveRecurringTransaction(
            id = null, name = "Test Mensual", amount = 10.0, description = null,
            categoryId = 1, transactionType = TransactionType.EXPENSE, startDate = startDate,
            frequency = RecurrenceFrequency.MONTHLY, interval = 1, dayOfMonth = 15,
            monthOfYear = null, endDate = null, isActive = true
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val savedTemplate = fakeRecurringTransactionRepository.templatesFlow.value.first()
        assertEquals(expectedNextDate, savedTemplate.nextOccurrenceDate)
    }

    @Test
    fun `deleteRecurringTransaction elimina la plantilla`() = runTest {
        // Preparación
        val template = RecurringTransaction(
            id = 1, name = "Para borrar", amount = 1.0, description = "Descripción",
            categoryId = 1, transactionType = TransactionType.EXPENSE, startDate = 0L,
            frequency = RecurrenceFrequency.DAILY, interval = 1, dayOfMonth = null,
            monthOfYear = null, endDate = null, nextOccurrenceDate = 1L,
            lastGeneratedDate = null, isActive = true
        )
        fakeRecurringTransactionRepository.insertRecurringTransaction(template)
        testDispatcher.scheduler.advanceUntilIdle()

        // Acción
        viewModel.deleteRecurringTransaction(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        assertTrue(fakeRecurringTransactionRepository.templatesFlow.value.isEmpty())
        assertEquals("Plantilla 'Para borrar' eliminada.", viewModel.uiState.value.userMessage)
    }
}