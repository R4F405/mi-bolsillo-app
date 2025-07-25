package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.ui.settings.theme.ThemeOption
import kotlinx.coroutines.flow.Flow
import java.util.Currency

/**
 * Interfaz que define las operaciones de acceso a datos para la configuración de la aplicación.
 * Proporciona métodos para obtener y guardar la moneda y el tema de la aplicación.
 */

interface SettingsRepository {
    val currency: Flow<Currency>
    suspend fun saveCurrency(currencyCode: String)

    val theme: Flow<ThemeOption>
    suspend fun saveTheme(theme: ThemeOption)

    val appLockEnabled: Flow<Boolean>
    suspend fun setAppLockEnabled(isEnabled: Boolean)
}