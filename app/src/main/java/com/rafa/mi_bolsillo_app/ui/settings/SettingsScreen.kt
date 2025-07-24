package com.rafa.mi_bolsillo_app.ui.settings

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rafa.mi_bolsillo_app.navigation.AppScreens
import com.rafa.mi_bolsillo_app.ui.settings.authentication.launchBiometricAuth
import com.rafa.mi_bolsillo_app.ui.settings.components.SettingsCategory
import com.rafa.mi_bolsillo_app.ui.settings.components.SettingsItem
import com.rafa.mi_bolsillo_app.ui.settings.components.ThemeSelectionDialog
import com.rafa.mi_bolsillo_app.ui.settings.components.SettingsSwitchItem
import com.rafa.mi_bolsillo_app.ui.theme.LocalIsDarkTheme

/**
 * Pantalla de configuración de la aplicación.
 * Permite al usuario ajustar preferencias como moneda, tema, idioma y más.
 *
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentDarkTheme = LocalIsDarkTheme.current
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    val appLockEnabled by viewModel.appLockEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            val topAppBarContainerColor = if (currentDarkTheme) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primary
            }
            val topAppBarContentColor = if (currentDarkTheme) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onPrimary
            }

            // Composición de la barra superior
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topAppBarContainerColor,
                    titleContentColor = topAppBarContentColor,
                    navigationIconContentColor = topAppBarContentColor
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(vertical = 8.dp)
        ) {
            SettingsCategory(title = "General y Apariencia")
            // Sección de moneda
            SettingsItem(
                title = "Moneda",
                subtitle = "Selecciona tu moneda local",
                icon = Icons.Default.MonetizationOn,
                onClick = { navController.navigate(AppScreens.CurrencySelectionScreen.route) }
            )
            // Sección de tema
            SettingsItem(
                title = "Tema de la Aplicación",
                subtitle = "Claro, oscuro o predeterminado del sistema",
                icon = Icons.Default.ColorLens,
                onClick = { showThemeDialog = true }
            )
            // Sección de idioma
            SettingsItem(
                title = "Idioma",
                subtitle = "Cambia el idioma de la aplicación",
                icon = Icons.Default.Language,
                onClick = { /* TODO: Navegar a HU-AJT-5 */ }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

            SettingsCategory(title = "Seguridad")
            // Sección de bloqueo de la aplicación
            SettingsSwitchItem(
                title = "Bloqueo de la Aplicación",
                subtitle = "Protege el acceso con PIN o huella",
                icon = Icons.Default.Lock,
                checked = appLockEnabled,
                onCheckedChange = { isEnabled ->
                    // Llamamos al gestor biométrico
                    launchBiometricAuth(
                        activity = context as AppCompatActivity,
                        onSuccess = {
                            // Si la autenticación es correcta, guardamos el nuevo estado
                            viewModel.setAppLockEnabled(isEnabled)
                        },
                        onError = { errorMessage ->
                            // Si hay un error, lo mostramos y no cambiamos el estado
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

            SettingsCategory(title = "Gestión de Datos")
            // Sección de exportación e importación de datos
            SettingsItem(
                title = "Exportar / Importar",
                subtitle = "Guarda o recupera tus transacciones",
                icon = Icons.Default.Storage,
                onClick = { /* TODO: Navegar a HU-AJT-8 y 9 */ }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

            SettingsCategory(title = "Acerca de")
            // Sección de información de la aplicación
            SettingsItem(
                title = "Información de la App",
                subtitle = "Versión, licencia y más",
                icon = Icons.Default.Info,
                onClick = { /* TODO: Navegar a HU-AJT-12, 13, 14 */ }
            )
        }
    }
    // Diálogo para seleccionar el tema
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { theme ->
                viewModel.saveTheme(theme)
                showThemeDialog = false
            }
        )
    }
}