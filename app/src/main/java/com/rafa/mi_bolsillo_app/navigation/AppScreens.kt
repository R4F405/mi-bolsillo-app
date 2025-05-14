package com.rafa.mi_bolsillo_app.navigation

sealed class AppScreens(val route: String) {
    object DashboardScreen : AppScreens("dashboard_screen")
    object TransactionHistoryScreen : AppScreens("transaction_history_screen")
    object AddTransactionScreen : AppScreens("add_transaction_screen?transactionId={transactionId}") {
        fun createRoute(transactionId: Long?) =
            if (transactionId != null && transactionId != -1L) "add_transaction_screen?transactionId=$transactionId"
            else "add_transaction_screen?transactionId=-1"
    }
    object CategoryManagementScreen : AppScreens("category_management_screen")
}