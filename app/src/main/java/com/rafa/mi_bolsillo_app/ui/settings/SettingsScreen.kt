package com.rafa.mi_bolsillo_app.ui.settings

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rafa.mi_bolsillo_app.navigation.AppScreens
import com.rafa.mi_bolsillo_app.ui.settings.authentication.launchBiometricAuth
import com.rafa.mi_bolsillo_app.ui.settings.theme.ThemeOption
import com.rafa.mi_bolsillo_app.ui.theme.LocalIsDarkTheme

/**
 * Pantalla de configuración de la aplicación.
 * Permite al usuario ajustar preferencias como moneda, tema, idioma y más.
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

// Composable para mostrar una categoría de configuración
@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(top = 8.dp)
    )
}

// Composable para un elemento de configuración
@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono del elemento de configuración
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Título y subtítulo del elemento de configuración
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Icono de flecha para indicar que se puede hacer clic
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Composable para el selector de tema
@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeOption,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeOption) -> Unit
) {
    // Diálogo para seleccionar el tema de la aplicación
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Tema") },
        text = {
            // Contenido del diálogo con opciones de tema
            Column {
                ThemeOption.values().forEach { theme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(theme.toDisplayString())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

// Extensión para convertir ThemeOption a una cadena de texto para mostrar
fun ThemeOption.toDisplayString(): String {
    return when (this) {
        ThemeOption.LIGHT -> "Claro"
        ThemeOption.DARK -> "Oscuro"
        ThemeOption.SYSTEM -> "Predeterminado del sistema"
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}