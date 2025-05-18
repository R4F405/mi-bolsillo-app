package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.data.local.dao.RecurringTransactionDao
import com.rafa.mi_bolsillo_app.data.local.entity.RecurringTransaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringTransactionRepositoryImpl @Inject constructor(
    private val recurringTransactionDao: RecurringTransactionDao
) : RecurringTransactionRepository {

    override fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>> =
        recurringTransactionDao.getAllRecurringTransactions()

    override fun getActiveRecurringTransactionsSortedByNextOccurrence(): Flow<List<RecurringTransaction>> =
        recurringTransactionDao.getActiveRecurringTransactionsSortedByNextOccurrence()

    override suspend fun getRecurringTransactionById(id: Long): RecurringTransaction? =
        recurringTransactionDao.getRecurringTransactionById(id)

    override suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction): Long {
        // Aquí podrías añadir lógica para calcular la primera nextOccurrenceDate si no viene calculada
        return recurringTransactionDao.insertRecurringTransaction(recurringTransaction)
    }

    override suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        // Lógica similar para recalcular nextOccurrenceDate si es necesario al actualizar
        recurringTransactionDao.updateRecurringTransaction(recurringTransaction)
    }

    override suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) =
        recurringTransactionDao.deleteRecurringTransaction(recurringTransaction)

    override suspend fun getDueRecurringTransactions(currentDate: Long): List<RecurringTransaction> =
        recurringTransactionDao.getDueRecurringTransactions(currentDate)
}