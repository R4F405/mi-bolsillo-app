package com.rafa.mi_bolsillo_app.data.repository

import kotlinx.coroutines.flow.Flow
import java.util.Currency

interface SettingsRepository {
    val currency: Flow<Currency>
    suspend fun saveCurrency(currencyCode: String)
}