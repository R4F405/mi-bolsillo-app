package com.rafa.mi_bolsillo_app.fakes

import com.rafa.mi_bolsillo_app.data.repository.SettingsRepository
import com.rafa.mi_bolsillo_app.ui.settings.theme.ThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Currency

/**
 * Una implementación falsa de [SettingsRepository] para fines de prueba.
 * Utiliza MutableStateFlow para simular el almacenamiento de la configuración
 * y permite guardar y recuperar la configuración de moneda, tema y bloqueo de aplicaciones.
 */

class FakeSettingsRepository: SettingsRepository {

    private val currencyFlow = MutableStateFlow(Currency.getInstance("EUR"))
    private val themeFlow = MutableStateFlow(ThemeOption.SYSTEM)
    private val appLockFlow = MutableStateFlow(false)

    override val currency: Flow<Currency> = currencyFlow.asStateFlow()
    override val theme: Flow<ThemeOption> = themeFlow.asStateFlow()
    override val appLockEnabled: Flow<Boolean> = appLockFlow.asStateFlow()

    override suspend fun saveCurrency(currencyCode: String) {
        currencyFlow.value = Currency.getInstance(currencyCode)
    }

    override suspend fun saveTheme(theme: ThemeOption) {
        themeFlow.value = theme
    }

    override suspend fun setAppLockEnabled(isEnabled: Boolean) {
        appLockFlow.value = isEnabled
    }

    // Funciones auxiliares para tests
    fun getSavedCurrencyCode(): String = currencyFlow.value.currencyCode
    fun getSavedTheme(): ThemeOption = themeFlow.value
    fun getAppLockEnabled(): Boolean = appLockFlow.value
}