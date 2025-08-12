package com.rafa.mi_bolsillo_app.ui.data_management

import com.rafa.mi_bolsillo_app.data.local.entity.Category
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@ExperimentalCoroutinesApi
class DataManagementImportViewModelTest {

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
    fun `importTransactionsFromCsv con csv valido importa las transacciones`() = runTest {
        // Preparación
        val date = dateFormat.format(Date())
        val csvContent = """
        Date,Amount,Description,Category,TransactionType
        "$date",150.0,"Bono extra","Ingresos Extra",INCOME
        "$date",25.99,"Cena con amigos","Ocio",EXPENSE
        """.trimIndent()

        // Acción
        viewModel.importTransactionsFromCsv(csvContent)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val transactions = fakeTransactionRepository.transactionsFlow.value
        assertEquals(2, transactions.size)
        assertEquals("Bono extra", transactions[0].description)
        assertEquals(150.0, transactions[0].amount, 0.0)
        assertEquals("2 transacciones importadas correctamente.", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `importTransactionsFromCsv con categorias nuevas crea las categorias e importa`() = runTest {
        // Preparación
        val date = dateFormat.format(Date())
        val csvContent = "\"$date\",75.0,\"Clases de guitarra\",\"Hobbies\",EXPENSE"

        // Acción
        viewModel.importTransactionsFromCsv("Date,Amount,Description,Category,TransactionType\n$csvContent")
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val categories = fakeCategoryRepository.categoriesFlow.value
        val transactions = fakeTransactionRepository.transactionsFlow.value
        assertTrue("La categoría 'Hobbies' debería haber sido creada", categories.any { it.name == "Hobbies" })
        assertEquals(1, transactions.size)
        assertEquals("Clases de guitarra", transactions.first().description)
    }

    @Test
    fun `importTransactionsFromCsv con categorias existentes reutiliza las categorias sin duplicar`() = runTest {
        // Preparación
        val comidaCategory = Category(id = 1, name = "Comida", colorHex = "#FF0000")
        fakeCategoryRepository.insertCategory(comidaCategory)
        testDispatcher.scheduler.advanceUntilIdle()
        val date = dateFormat.format(Date())
        val csvContent = "\"$date\",15.50,\"Almuerzo de trabajo\",\"comida\",EXPENSE" // Nótese la minúscula

        // Acción
        viewModel.importTransactionsFromCsv("Date,Amount,Description,Category,TransactionType\n$csvContent")
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val categories = fakeCategoryRepository.categoriesFlow.value
        val transactions = fakeTransactionRepository.transactionsFlow.value
        assertEquals("No se debería crear una nueva categoría", 1, categories.size)
        assertEquals("La transacción debe usar el ID de la categoría existente", 1L, transactions.first().categoryId)
    }

    @Test
    fun `importTransactionsFromCsv con csv vacio muestra mensaje de error`() = runTest {
        // Preparación
        val csvContent = "Date,Amount,Description,Category,TransactionType\n"

        // Acción
        viewModel.importTransactionsFromCsv(csvContent)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        assertEquals("El archivo CSV está vacío o no tiene datos.", viewModel.uiState.value.userMessage)
        assertTrue(fakeTransactionRepository.transactionsFlow.value.isEmpty())
    }

    @Test
    fun `importTransactionsFromCsv con linea malformada la ignora y continua`() = runTest {
        // Preparación
        val date = dateFormat.format(Date())
        val csvContent = """
        Date,Amount,Description,Category,TransactionType
        "$date",100.0,"Válido 1","Categoría 1",INCOME
        línea-rota-sin-comas
        "$date",200.0,"Válido 2","Categoría 2",EXPENSE
        """.trimIndent()

        // Acción
        viewModel.importTransactionsFromCsv(csvContent)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val transactions = fakeTransactionRepository.transactionsFlow.value
        assertEquals("Debería haber importado solo las 2 líneas válidas", 2, transactions.size)
        assertEquals("Válido 1", transactions[0].description)
        assertEquals("Válido 2", transactions[1].description)
    }

    @Test
    fun `importTransactionsFromCsv con formato de fecha invalido ignora la transaccion`() = runTest {
        // Preparación
        val csvContent = "25/12/2025,50.0,\"Regalo de Navidad\",\"Regalos\",EXPENSE" // Formato incorrecto

        // Acción
        viewModel.importTransactionsFromCsv("Date,Amount,Description,Category,TransactionType\n$csvContent")
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val transactions = fakeTransactionRepository.transactionsFlow.value
        assertTrue("No debería importar transacciones con formato de fecha incorrecto", transactions.isEmpty())
    }
}