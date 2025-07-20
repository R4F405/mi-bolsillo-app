package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.data.local.entity.RecurringTransaction
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define las operaciones de acceso a datos para las transacciones recurrentes.
 * Proporciona métodos para obtener, insertar, actualizar y eliminar transacciones recurrentes,
 * así como para obtener transacciones activas y vencidas.
 */

interface RecurringTransactionRepository {
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>
    fun getActiveRecurringTransactionsSortedByNextOccurrence(): Flow<List<RecurringTransaction>>
    suspend fun getRecurringTransactionById(id: Long): RecurringTransaction?
    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction): Long
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction)
    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction)
    suspend fun getDueRecurringTransactions(currentDate: Long): List<RecurringTransaction>
}