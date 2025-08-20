package com.rafa.mi_bolsillo_app.ui.settings.about

import androidx.lifecycle.ViewModel
import com.rafa.mi_bolsillo_app.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla "Acerca de".
 *
 * @property appVersion La versión actual de la aplicación.
 */
data class AboutUiState(
    val appVersion: String = ""
)

/**
 * ViewModel para la pantalla "Acerca de".
 *
 * Se encarga de proporcionar la información necesaria a la UI, como la versión de la app.
 */
@HiltViewModel
class AboutViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AboutUiState())
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()

    init {
        loadAppVersion()
    }

    /**
     * Carga la versión de la aplicación desde BuildConfig y actualiza el estado de la UI.
     */
    private fun loadAppVersion() {
        // Obtenemos el versionName definido en el build.gradle.kts del módulo app
        _uiState.value = _uiState.value.copy(appVersion = BuildConfig.VERSION_NAME)
    }
}
