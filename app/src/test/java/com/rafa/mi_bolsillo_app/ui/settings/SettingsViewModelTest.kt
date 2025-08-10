package com.rafa.mi_bolsillo_app.ui.settings

import com.rafa.mi_bolsillo_app.fakes.FakeSettingsRepository
import com.rafa.mi_bolsillo_app.ui.settings.theme.ThemeOption
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
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SettingsViewModel
    private lateinit var fakeSettingsRepository: FakeSettingsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSettingsRepository = FakeSettingsRepository()
        viewModel = SettingsViewModel(fakeSettingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveCurrency llama al repositorio con el codigo de moneda correcto`() = runTest {
        // Acción
        viewModel.saveCurrency("USD")
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        assertEquals("USD", fakeSettingsRepository.getSavedCurrencyCode())
    }

    @Test
    fun `saveTheme llama al repositorio con la opcion de tema correcta`() = runTest {
        // Acción
        viewModel.saveTheme(ThemeOption.DARK)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        assertEquals(ThemeOption.DARK, fakeSettingsRepository.getSavedTheme())
    }

    @Test
    fun `setAppLockEnabled llama al repositorio con el estado de bloqueo correcto`() = runTest {
        // Acción
        viewModel.setAppLockEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificación
        assertEquals(true, fakeSettingsRepository.getAppLockEnabled())
    }
}