package com.rafa.mi_bolsillo_app.ui.settings.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rafa.mi_bolsillo_app.ui.settings.components.SettingsItem
import com.rafa.mi_bolsillo_app.ui.theme.LocalIsDarkTheme

/**
 * Pantalla que muestra la información "Acerca de" la aplicación.
 * Proporciona detalles como la versión de la app, un enlace para valorarla y la licencia.
 *
 * @param navController El controlador de navegación para manejar el retroceso.
 * @param viewModel El ViewModel asociado a esta pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
    viewModel: AboutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentDarkTheme = LocalIsDarkTheme.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            val topAppBarContainerColor = if (currentDarkTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
            val topAppBarContentColor = if (currentDarkTheme) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary

            // Barra superior con título y botón de navegación para volver
            TopAppBar(
                title = { Text("Información de la App") },
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
            // Elemento para mostrar la versión de la app
            SettingsItem(
                title = "Versión de la Aplicación",
                subtitle = uiState.appVersion,
                icon = Icons.Default.Info,
                onClick = {}
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

            // Elemento para valorar la aplicación en la Play Store
            SettingsItem(
                title = "Valora esta aplicación",
                subtitle = "Tu opinión nos ayuda a mejorar",
                icon = Icons.Default.StarRate,
                onClick = {
                    openPlayStoreForRating(context)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

            // Elemento para mostrar la licencia de la aplicación
            SettingsItem(
                title = "Licencia",
                subtitle = "GNU General Public License v3.0",
                icon = Icons.Default.Description,
                onClick = {
                    openUrl(context, "https://www.gnu.org/licenses/gpl-3.0.html")
                }
            )
        }
    }
}

/**
 * Abre la página de la aplicación en la Play Store para que el usuario pueda valorarla.
 *
 * @param context El contexto de la aplicación.
 */
private fun openPlayStoreForRating(context: Context) {
//    val packageName = context.packageName
//    try {
//        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
//    } catch (e: android.content.ActivityNotFoundException) {
//        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
//    }
}

/**
 * Abre una URL en el navegador web del dispositivo.
 *
 * @param context El contexto de la aplicación.
 * @param url La URL que se va a abrir.
 */
private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
