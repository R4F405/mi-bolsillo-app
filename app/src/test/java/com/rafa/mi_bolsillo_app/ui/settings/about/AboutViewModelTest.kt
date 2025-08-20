package com.rafa.mi_bolsillo_app.ui.settings.about

import com.rafa.mi_bolsillo_app.BuildConfig
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

/**
* Tests unitarios para el AboutViewModel.
*
* Se verifica que el ViewModel cargue y exponga correctamente la información
* de la aplicación, como la versión.
*/
@ExperimentalCoroutinesApi
class AboutViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: AboutViewModel

    @Before
    fun setUp() {
        // Establecemos el dispatcher principal para los tests de coroutines
        Dispatchers.setMain(testDispatcher)
        viewModel = AboutViewModel()
    }

    @After
    fun tearDown() {
        // Reseteamos el dispatcher principal después de cada test
        Dispatchers.resetMain()
    }

    @Test
    fun `al iniciar - el uiState contiene la version correcta de la app`() = runTest {
        // Avanzamos el scheduler para asegurarnos de que el bloque init se complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        val expectedVersion = BuildConfig.VERSION_NAME
        val actualVersion = viewModel.uiState.value.appVersion

        assertEquals("La versión en el uiState debe coincidir con la de BuildConfig", expectedVersion, actualVersion)

    }
}