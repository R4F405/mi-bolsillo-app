package com.rafa.mi_bolsillo_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rafa.mi_bolsillo_app.navigation.AppScreens // <-- IMPORTA TUS RUTAS
import com.rafa.mi_bolsillo_app.ui.theme.MiBolsilloAppTheme
import com.rafa.mi_bolsillo_app.ui.transactions.TransactionListScreen // Lo usaremos como historial
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.rafa.mi_bolsillo_app.ui.dashboard.DashboardScreen // <-- IMPORTA DashboardScreen
import com.rafa.mi_bolsillo_app.ui.transactions.TransactionListScreen
import com.rafa.mi_bolsillo_app.ui.add_transaction.AddTransactionScreen
import androidx.navigation.NavType                     // <-- AÑADE IMPORT
import androidx.navigation.navArgument               // <-- AÑADE IMPORT
import com.rafa.mi_bolsillo_app.ui.add_transaction.AddTransactionScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiBolsilloAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = AppScreens.DashboardScreen.route
                    ) {
                        composable(route = AppScreens.DashboardScreen.route) {
                            DashboardScreen(navController = navController)
                        }
                        composable(route = AppScreens.TransactionHistoryScreen.route) {
                            TransactionListScreen(navController = navController)
                        }

                        // Configuración para AddTransactionScreen con argumento opcional
                        composable(
                            route = AppScreens.AddTransactionScreen.route, // La ruta base con el placeholder
                            arguments = listOf(
                                navArgument("transactionId") {
                                    type = NavType.LongType
                                    defaultValue = -1L // Valor por defecto para "nueva transacción"
                                }
                            )
                        ) { navBackStackEntry ->
                            val transactionId = navBackStackEntry.arguments?.getLong("transactionId") ?: -1L
                            AddTransactionScreen(
                                navController = navController,
                                transactionId = transactionId // Pasamos el ID a la pantalla
                            )
                        }
                        // ... otras rutas futuras ...
                    }
                }
            }
        }
    }
}