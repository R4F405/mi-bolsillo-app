package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.data.local.dao.ExpenseByCategory
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para el repositorio de transacciones.
 *
 * Proporciona métodos para interactuar con la tabla de transacciones en la base de datos.
 *
 */

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>> // Obtener todas las transacciones
    fun getTransactionsByType(transactionType: TransactionType): Flow<List<Transaction>> // Obtener transacciones por tipo
    fun getTransactionsByCategoryId(categoryId: Long): Flow<List<Transaction>> // Obtener transacciones por categoría
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> // Obtener transacciones por rango de fechas
    suspend fun getTransactionById(id: Long): Transaction? // Obtener una transacción por su ID
    suspend fun insertTransaction(transaction: Transaction) // Insertar una nueva transacción
    suspend fun updateTransaction(transaction: Transaction) // Actualizar una transacción
    suspend fun deleteTransaction(transaction: Transaction) // Eliminar una transacción
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<Transaction>> // Obtener transacciones por rango de fechas
    fun getTotalIncomeBetweenDates(startDate: Long, endDate: Long): Flow<Double?> // Obtener el total de ingresos por rango de fechas
    fun getTotalExpensesBetweenDates(startDate: Long, endDate: Long): Flow<Double?> // Obtener el total de gastos por rango de fechas
    fun getExpensesByCategoryInRange(startDate: Long, endDate: Long): Flow<List<ExpenseByCategory>> // Obtener gastos por categoría en un rango de fechas
}