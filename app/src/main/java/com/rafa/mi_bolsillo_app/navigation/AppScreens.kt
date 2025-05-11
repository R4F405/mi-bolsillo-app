package com.rafa.mi_bolsillo_app.navigation

sealed class AppScreens(val route: String) {
    object DashboardScreen : AppScreens("dashboard_screen")
    object TransactionHistoryScreen : AppScreens("transaction_history_screen")
    object AddTransactionScreen : AppScreens("add_transaction_screen") // Para la pantalla "Nueva Transacción"
}