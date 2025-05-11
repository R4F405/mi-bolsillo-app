package com.rafa.mi_bolsillo_app.navigation

sealed class AppScreens(val route: String) {
    object DashboardScreen : AppScreens("dashboard_screen")
    object TransactionHistoryScreen : AppScreens("transaction_history_screen")

    // Hacemos transactionId opcional y definimos un nombre para el argumento.
    // Si transactionId no se pasa, usaremos un valor por defecto (ej. -1L) para indicar "nueva transacción".
    object AddTransactionScreen : AppScreens("add_transaction_screen?transactionId={transactionId}") {
        fun createRoute(transactionId: Long?) =
            if (transactionId != null && transactionId != -1L) "add_transaction_screen?transactionId=$transactionId"
            else "add_transaction_screen?transactionId=-1" // -1L para indicar nueva transacción
    }
    // ... otras pantallas futuras ...
}