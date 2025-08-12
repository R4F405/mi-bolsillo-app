package com.rafa.mi_bolsillo_app.ui.data_management

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rafa.mi_bolsillo_app.ui.settings.components.SettingsItem
import com.rafa.mi_bolsillo_app.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter

/**
 * Pantalla para gestionar la importación y exportación de datos.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    navController: NavController,
    viewModel: DataManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentDarkTheme = LocalIsDarkTheme.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Launcher para exportar (crear documento)
    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    try {
                        val csvContent = viewModel.getCsvContent()
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            OutputStreamWriter(outputStream).use { writer ->
                                writer.write(csvContent)
                            }
                        }
                        snackbarHostState.showSnackbar("Exportación completada.")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error al exportar: ${e.message}")
                    }
                }
            }
        }
    )

    // Usamos OpenDocument
    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                try {
                    val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { it.readText() }
                    if (content != null) {
                        viewModel.importTransactionsFromCsv(content)
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("No se pudo leer el archivo.")
                        }
                    }
                } catch (e: Exception) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Error al leer el archivo: ${e.message}")
                    }
                }
            }
        }
    )

    // Efecto para mostrar mensajes del ViewModel
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val topAppBarContainerColor = if (currentDarkTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
            val topAppBarContentColor = if (currentDarkTheme) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary

            TopAppBar(
                title = { Text("Gestión de Datos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topAppBarContainerColor,
                    titleContentColor = topAppBarContentColor,
                    navigationIconContentColor = topAppBarContentColor,
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
            SettingsItem(
                title = "Exportar Transacciones",
                subtitle = "Guarda tu historial en un archivo CSV",
                icon = Icons.Default.FileDownload,
                onClick = {
                    csvExportLauncher.launch("mi_bolsillo_export.csv")
                }
            )
            SettingsItem(
                title = "Importar Transacciones",
                subtitle = "Recupera tus datos desde un archivo CSV",
                icon = Icons.Default.FileUpload,
                onClick = {
                    // Pasamos un array de tipos MIME
                    csvImportLauncher.launch(arrayOf("text/csv", "text/comma-separated-values"))
                }
            )
        }
    }
}