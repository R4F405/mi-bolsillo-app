package com.rafa.mi_bolsillo_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rafa.mi_bolsillo_app.navigation.AppScreens // <-- IMPORTA TUS RUTAS
import com.rafa.mi_bolsillo_app.ui.theme.MiBolsilloAppTheme
import com.rafa.mi_bolsillo_app.ui.transactions.TransactionListScreen // Lo usaremos como historial

import dagger.hilt.android.AndroidEntryPoint

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
                    // Configura el controlador de navegación
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = AppScreens.DashboardScreen.route // Empezamos en el Dashboard
                    ) {
                        composable(route = AppScreens.DashboardScreen.route) {
                            // Aquí irá tu DashboardScreen Composable
                            // Por ahora, un placeholder:
                            // DashboardScreen(navController = navController)
                            // Lo reemplazaremos por el TransactionListScreen temporalmente para probar la navegación
                            TransactionListScreen(navController = navController) // Temporal, para ver que funciona
                        }
                        composable(route = AppScreens.TransactionHistoryScreen.route) {
                            // Nuestra TransactionListScreen actuará como el historial
                            TransactionListScreen(navController = navController)
                        }
                        // Aquí podrías añadir más destinos, como una pantalla para añadir/editar transacciones
                    }
                }
            }
        }
    }
}