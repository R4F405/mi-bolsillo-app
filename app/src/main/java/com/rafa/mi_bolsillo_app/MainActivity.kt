package com.rafa.mi_bolsillo_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rafa.mi_bolsillo_app.navigation.AppScreens
import com.rafa.mi_bolsillo_app.ui.theme.MiBolsilloAppTheme
import dagger.hilt.android.AndroidEntryPoint
import com.rafa.mi_bolsillo_app.ui.dashboard.DashboardScreen
import com.rafa.mi_bolsillo_app.ui.add_transaction.AddTransactionScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.rafa.mi_bolsillo_app.ui.budget.BudgetScreen
import com.rafa.mi_bolsillo_app.ui.category_management.CategoryManagementScreen
import com.rafa.mi_bolsillo_app.ui.recurring_transactions.RecurringTransactionListScreen
import com.rafa.mi_bolsillo_app.ui.transactions.TransactionListScreen
import com.rafa.mi_bolsillo_app.ui.settings.SettingsScreen
import com.rafa.mi_bolsillo_app.ui.settings.CurrencySelectionScreen

/**
 * Clase principal de la aplicación.
 *
 */

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
                        composable(route = AppScreens.CurrencySelectionScreen.route) {
                            CurrencySelectionScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}