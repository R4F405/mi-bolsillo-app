package com.rafa.mi_bolsillo_app.ui.data_management

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
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@ExperimentalCoroutinesApi
class DataManagementExportViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: DataManagementViewModel
    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeTransactionRepository = FakeTransactionRepository()
        fakeCategoryRepository = FakeCategoryRepository()
        viewModel = DataManagementViewModel(fakeTransactionRepository, fakeCategoryRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getCsvContent cuando no hay transacciones retorna solo la cabecera`() = runTest {
        // Acción
        val csvContent = viewModel.getCsvContent()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val expectedHeader = "Date,Amount,Description,Category,TransactionType\n"
        assertEquals(expectedHeader, csvContent)
    }

    @Test
    fun `getCsvContent con transacciones retorna formato CSV correcto`() = runTest {
        // Preparación
        val date1 = System.currentTimeMillis()
        val category1 = Category(id = 1, name = "Salario", colorHex = "#00FF00")
        val transaction1 = Transaction(id = 1, date = date1, amount = 2000.0, description = "Nómina", categoryId = 1, transactionType = TransactionType.INCOME)

        val date2 = date1 - 86400000 // un día antes
        val category2 = Category(id = 2, name = "Comida", colorHex = "#FF0000")
        val transaction2 = Transaction(id = 2, date = date2, amount = 50.0, description = "Supermercado", categoryId = 2, transactionType = TransactionType.EXPENSE)

        fakeCategoryRepository.insertCategories(listOf(category1, category2))
        fakeTransactionRepository.insertTransaction(transaction1)
        fakeTransactionRepository.insertTransaction(transaction2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Acción
        val csvContent = viewModel.getCsvContent()
        testDispatcher.scheduler.advanceUntilIdle()


        // Verificación
        val expectedCsv = """
        Date,Amount,Description,Category,TransactionType
        "${dateFormat.format(Date(date1))}",2000.0,"Nómina","Salario",INCOME
        "${dateFormat.format(Date(date2))}",50.0,"Supermercado","Comida",EXPENSE
        """.trimIndent() + "\n"


        assertEquals(expectedCsv, csvContent)
    }

    @Test
    fun `getCsvContent con comas en descripcion maneja las comillas correctamente`() = runTest {
        // Preparación
        val date = System.currentTimeMillis()
        val category = Category(id = 1, name = "Compras", colorHex = "#0000FF")
        val transaction = Transaction(id = 1, date = date, amount = 25.0, description = "Regalos, tarjetas, y más", categoryId = 1, transactionType = TransactionType.EXPENSE)

        fakeCategoryRepository.insertCategory(category)
        fakeTransactionRepository.insertTransaction(transaction)
        testDispatcher.scheduler.advanceUntilIdle()

        // Acción
        val csvContent = viewModel.getCsvContent()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val expectedLine = "\"${dateFormat.format(Date(date))}\",25.0,\"Regalos, tarjetas, y más\",\"Compras\",EXPENSE\n"
        assertEquals("Date,Amount,Description,Category,TransactionType\n$expectedLine", csvContent)
    }

    @Test
    fun `getCsvContent con descripcion nula la exporta como campo vacio`() = runTest {
        // Preparación
        val date = System.currentTimeMillis()
        val category = Category(id = 1, name = "Transporte", colorHex = "#FFFF00")
        val transaction = Transaction(id = 1, date = date, amount = 12.0, description = null, categoryId = 1, transactionType = TransactionType.EXPENSE)

        fakeCategoryRepository.insertCategory(category)
        fakeTransactionRepository.insertTransaction(transaction)
        testDispatcher.scheduler.advanceUntilIdle()

        // Acción
        val csvContent = viewModel.getCsvContent()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val expectedLine = "\"${dateFormat.format(Date(date))}\",12.0,\"\",\"Transporte\",EXPENSE\n"
        assertEquals("Date,Amount,Description,Category,TransactionType\n$expectedLine", csvContent)
    }
}