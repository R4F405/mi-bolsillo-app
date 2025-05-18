package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.data.local.entity.RecurringTransaction
import kotlinx.coroutines.flow.Flow

interface RecurringTransactionRepository {
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>
    fun getActiveRecurringTransactionsSortedByNextOccurrence(): Flow<List<RecurringTransaction>>
    suspend fun getRecurringTransactionById(id: Long): RecurringTransaction?
    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction): Long
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction)
    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction)
    suspend fun getDueRecurringTransactions(currentDate: Long): List<RecurringTransaction>
}