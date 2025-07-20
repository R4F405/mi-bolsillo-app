package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.data.local.dao.RecurringTransactionDao
import com.rafa.mi_bolsillo_app.data.local.entity.RecurringTransaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de transacciones recurrentes.
 * Proporciona acceso a los datos de transacciones recurrentes a través de la capa de persistencia.
 * Utiliza el DAO de transacciones recurrentes para realizar operaciones CRUD y consultas específicas.
 */

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
        return recurringTransactionDao.insertRecurringTransaction(recurringTransaction)
    }

    override suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        recurringTransactionDao.updateRecurringTransaction(recurringTransaction)
    }

    override suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) =
        recurringTransactionDao.deleteRecurringTransaction(recurringTransaction)

    override suspend fun getDueRecurringTransactions(currentDate: Long): List<RecurringTransaction> =
        recurringTransactionDao.getDueRecurringTransactions(currentDate)
}