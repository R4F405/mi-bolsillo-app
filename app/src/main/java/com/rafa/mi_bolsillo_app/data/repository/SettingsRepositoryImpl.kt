package com.rafa.mi_bolsillo_app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object PreferencesKeys {
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
    }

    override val currency: Flow<Currency> = dataStore.data.map { preferences ->
        val currencyCode = preferences[PreferencesKeys.CURRENCY_CODE]
        if (currencyCode != null) {
            Currency.getInstance(currencyCode)
        } else {
            // Moneda por defecto EUR si no hay nada guardado
            Currency.getInstance("EUR")
        }
    }

    override suspend fun saveCurrency(currencyCode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY_CODE] = currencyCode
        }
    }
}