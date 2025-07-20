package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.ui.settings.theme.ThemeOption
import kotlinx.coroutines.flow.Flow
import java.util.Currency

interface SettingsRepository {
    val currency: Flow<Currency>
    suspend fun saveCurrency(currencyCode: String)

    val theme: Flow<ThemeOption>
    suspend fun saveTheme(theme: ThemeOption)
}