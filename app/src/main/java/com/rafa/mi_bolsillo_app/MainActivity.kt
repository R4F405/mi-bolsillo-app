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
                            DashboardScreen(navController = navController) // <-- USA EL REAL
                        }
                        composable(route = AppScreens.TransactionHistoryScreen.route) {
                            TransactionListScreen(navController = navController)
                        }
                        composable(route = AppScreens.AddTransactionScreen.route) {
                            // Placeholder para la pantalla de añadir transacción
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Pantalla Añadir Transacción (Próximamente)")
                            }
                        }
                        // ... otras rutas futuras ...
                    }
                }
            }
        }
    }
}