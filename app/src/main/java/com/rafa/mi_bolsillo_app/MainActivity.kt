package com.rafa.mi_bolsillo_app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rafa.mi_bolsillo_app.data.repository.SettingsRepository
import com.rafa.mi_bolsillo_app.navigation.AppScreens
import com.rafa.mi_bolsillo_app.ui.budget.BudgetScreen
import com.rafa.mi_bolsillo_app.ui.category_management.CategoryManagementScreen
import com.rafa.mi_bolsillo_app.ui.dashboard.DashboardScreen
import com.rafa.mi_bolsillo_app.ui.data_management.DataManagementScreen
import com.rafa.mi_bolsillo_app.ui.recurring_transactions.RecurringTransactionListScreen
import com.rafa.mi_bolsillo_app.ui.settings.SettingsScreen
import com.rafa.mi_bolsillo_app.ui.settings.about.AboutScreen
import com.rafa.mi_bolsillo_app.ui.settings.currency.CurrencySelectionScreen
import com.rafa.mi_bolsillo_app.ui.settings.authentication.launchBiometricAuth
import com.rafa.mi_bolsillo_app.ui.theme.MiBolsilloAppTheme
import com.rafa.mi_bolsillo_app.ui.transactions.AddTransactionScreen
import com.rafa.mi_bolsillo_app.ui.transactions.TransactionListScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Clase principal de la aplicación.
 * Inicializa la actividad, configura el tema y gestiona el bloqueo de seguridad.
 * Define las rutas de navegación utilizando Jetpack Compose Navigation.
 *
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() { // CAMBIO: Hereda de AppCompatActivity

    @Inject
    lateinit var settingsRepository: SettingsRepository

    // Variable para controlar si la app debe re-bloquearse al volver a primer plano
    private var shouldLockOnResume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Estado para saber si la app está bloqueada
        val isLocked = mutableStateOf(true)

        // Comprobar el estado del bloqueo al crear la actividad
        lifecycleScope.launch {
            val appLockEnabled = settingsRepository.appLockEnabled.first()
            if (!appLockEnabled) {
                isLocked.value = false // Si no está activado, desbloqueamos directamente
            }
        }

        setContent {
            MiBolsilloAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLocked.value) {
                        // Si está bloqueada, mostramos la pantalla de bloqueo
                        AppLockScreen(
                            onUnlock = {
                                launchBiometricAuth(
                                    activity = this@MainActivity,
                                    onSuccess = {
                                        isLocked.value = false
                                        // Asegurarse de que el flag de re-bloqueo esté bajo
                                        shouldLockOnResume = false
                                    },
                                    onError = { errorMsg ->
                                        // Opcional: mostrar un Toast o manejar el error si la autenticación falla repetidamente.
                                        // Por ahora, el usuario puede simplemente volver a intentarlo.
                                        Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        )
                    } else {
                        // Si no, mostramos el contenido normal de la app
                        MainAppContent()
                    }
                }
            }
        }

        // Observador del ciclo de vida para volver a bloquear la app
        lifecycle.addObserver(androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                // Cuando la app pasa a segundo plano, marcamos que debe bloquearse
                shouldLockOnResume = true
            }
            if (event == Lifecycle.Event.ON_RESUME && shouldLockOnResume) {
                // Cuando vuelve, si está marcada y el ajuste está activado, la bloqueamos
                lifecycleScope.launch {
                    if (settingsRepository.appLockEnabled.first()) {
                        isLocked.value = true
                    }
                }
                shouldLockOnResume = false // Reseteamos la marca
            }
        })
    }
}

// NUEVO COMPOSABLE: Pantalla de Bloqueo
@Composable
fun AppLockScreen(onUnlock: () -> Unit) {
    // Lanzar la autenticación automáticamente al mostrar la pantalla
    LaunchedEffect(Unit) {
        onUnlock()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onUnlock) {
            Text("Desbloquear Aplicación")
        }
    }
}

// NUEVO COMPOSABLE: Contenido principal de la app (lo que ya tenías)
@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppScreens.DashboardScreen.route
    ) {
        // Ruta para la pantalla del dashboard
        composable(route = AppScreens.DashboardScreen.route) {
            DashboardScreen(navController = navController)
        }
        // Ruta para la pantalla de historial de transacciones
        composable(route = AppScreens.TransactionHistoryScreen.route) {
            TransactionListScreen(navController = navController)
        }
        // Ruta para la pantalla de añadir o editar transacciones
        composable(
            route = AppScreens.AddTransactionScreen.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType; defaultValue = -1L })
        ) { navBackStackEntry ->
            val transactionId = navBackStackEntry.arguments?.getLong("transactionId") ?: -1L
            AddTransactionScreen(navController = navController, transactionId = transactionId)
        }
        // Ruta para la pantalla de gestión de categorías
        composable(route = AppScreens.CategoryManagementScreen.route) {
            CategoryManagementScreen(navController = navController)
        }
        // Ruta para la pantalla de transacciones recurrentes
        composable(route = AppScreens.RecurringTransactionListScreen.route) {
            RecurringTransactionListScreen(navController = navController)
        }
        // Ruta para la pantalla de presupuestos
        composable(route = AppScreens.BudgetScreen.route) {
            BudgetScreen(navController = navController)
        }
        // Ruta para la pantalla de ajustes
        composable(route = AppScreens.SettingsScreen.route) {
            SettingsScreen(navController = navController)
        }
        // Ruta para la pantalla de selección de moneda
        composable(route = AppScreens.CurrencySelectionScreen.route) {
            CurrencySelectionScreen(navController = navController)
        }
        // Ruta para la pantalla de gestión de datos
        composable(route = AppScreens.DataManagementScreen.route) {
            DataManagementScreen(navController = navController)
        }
        // Ruta para la pantalla "Acerca de"
        composable(route = AppScreens.AboutScreen.route) {
            AboutScreen(navController = navController)
        }
    }
}
