package com.rafa.mi_bolsillo_app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafa.mi_bolsillo_app.data.repository.SettingsRepository
import com.rafa.mi_bolsillo_app.ui.settings.theme.ThemeOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Currency
import javax.inject.Inject

/**
 * ViewModel para la pantalla de configuración de la aplicación.
 * Permite a los usuarios cambiar la moneda y el tema de la aplicación.
 *
 */

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // Exponer el estado de la moneda y el tema como StateFlows
    val currentCurrency = settingsRepository.currency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Currency.getInstance("EUR")
        )

    // Exponer el estado del tema como StateFlow
    val currentTheme = settingsRepository.theme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeOption.SYSTEM
        )

    // Funciones para guardar la moneda seleccionada
    fun saveCurrency(currencyCode: String) {
        viewModelScope.launch {
            settingsRepository.saveCurrency(currencyCode)
        }
    }

    // Función para guardar el tema seleccionado
    fun saveTheme(theme: ThemeOption) {
        viewModelScope.launch {
            settingsRepository.saveTheme(theme)
        }
    }

    val appLockEnabled = settingsRepository.appLockEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setAppLockEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAppLockEnabled(isEnabled)
        }
    }
}