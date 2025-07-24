package com.rafa.mi_bolsillo_app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rafa.mi_bolsillo_app.ui.settings.theme.ThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Currency
import javax.inject.Inject

/**
 * Implementación del repositorio de configuración de la aplicación.
 * Proporciona acceso a la configuración de moneda y tema a través de DataStore.
 */

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object PreferencesKeys {
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val THEME = stringPreferencesKey("theme_option")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")

    }

    override val currency: Flow<Currency> = dataStore.data.map { preferences ->
        val currencyCode = preferences[PreferencesKeys.CURRENCY_CODE]
        if (currencyCode != null) {
            Currency.getInstance(currencyCode)
        } else {
            Currency.getInstance("EUR")
        }
    }

    override suspend fun saveCurrency(currencyCode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY_CODE] = currencyCode
        }
    }

    override val theme: Flow<ThemeOption> = dataStore.data.map { preferences ->
        when (preferences[PreferencesKeys.THEME]) {
            ThemeOption.LIGHT.name -> ThemeOption.LIGHT
            ThemeOption.DARK.name -> ThemeOption.DARK
            else -> ThemeOption.SYSTEM
        }
    }

    override suspend fun saveTheme(theme: ThemeOption) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    override val appLockEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.APP_LOCK_ENABLED] ?: false
    }

    override suspend fun setAppLockEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LOCK_ENABLED] = isEnabled
        }
    }
}