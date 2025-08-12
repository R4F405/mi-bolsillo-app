package com.rafa.mi_bolsillo_app.navigation

/**
 * Clase sellada que define las pantallas de la aplicación.
 * Cada objeto dentro de esta clase representa una pantalla con su ruta correspondiente.
 */

sealed class AppScreens(val route: String) {
    object DashboardScreen : AppScreens("dashboard_screen")
    object TransactionHistoryScreen : AppScreens("transaction_history_screen")
    object AddTransactionScreen : AppScreens("add_transaction_screen?transactionId={transactionId}") {
        fun createRoute(transactionId: Long?) =
            if (transactionId != null && transactionId != -1L) "add_transaction_screen?transactionId=$transactionId"
            else "add_transaction_screen?transactionId=-1"
    }
    object CategoryManagementScreen : AppScreens("category_management_screen")
    object RecurringTransactionListScreen : AppScreens("recurring_transaction_list_screen")
    object BudgetScreen : AppScreens("budget_screen")
    object CurrencySelectionScreen : AppScreens("currency_selection_screen")
    object SettingsScreen : AppScreens("settings_screen")
    object DataManagementScreen : AppScreens("data_management_screen")
}