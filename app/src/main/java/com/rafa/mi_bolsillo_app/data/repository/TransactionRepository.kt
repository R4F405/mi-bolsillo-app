package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.data.local.dao.ExpenseByCategory
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define las operaciones de acceso a datos para las transacciones.
 * Proporciona métodos para obtener, insertar, actualizar y eliminar transacciones,
 * así como para obtener transacciones por tipo, categoría y rango de fechas.
 */
interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun getAllTransactionsList(): List<Transaction>
    fun getTransactionsByType(transactionType: TransactionType): Flow<List<Transaction>>
    fun getTransactionsByCategoryId(categoryId: Long): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<Transaction>>
    fun getTotalIncomeBetweenDates(startDate: Long, endDate: Long): Flow<Double?>
    fun getTotalExpensesBetweenDates(startDate: Long, endDate: Long): Flow<Double?>
    fun getExpensesByCategoryInRange(startDate: Long, endDate: Long): Flow<List<ExpenseByCategory>>
}