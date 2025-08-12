package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.data.local.dao.ExpenseByCategory
import com.rafa.mi_bolsillo_app.data.local.dao.TransactionDao
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de transacciones.
 * Proporciona acceso a los datos de transacciones a través de la capa de persistencia.
 * Utiliza el DAO de transacciones para realizar operaciones CRUD y consultas específicas.
 */

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    override suspend fun getAllTransactionsList(): List<Transaction> = transactionDao.getAllTransactions().first()
    override fun getTransactionsByType(transactionType: TransactionType): Flow<List<Transaction>> = transactionDao.getTransactionsByType(transactionType)
    override fun getTransactionsByCategoryId(categoryId: Long): Flow<List<Transaction>> = transactionDao.getTransactionsByCategoryId(categoryId)
    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> = transactionDao.getTransactionsByDateRange(startDate, endDate)
    override suspend fun getTransactionById(id: Long): Transaction? = transactionDao.getTransactionById(id)
    override suspend fun insertTransaction(transaction: Transaction) = transactionDao.insertTransaction(transaction)
    override suspend fun updateTransaction(transaction: Transaction) = transactionDao.updateTransaction(transaction)
    override suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction)

    // Obtener transacciones por rango de fechas
    override fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate)
    }

    // Obtener el total de ingresos por rango de fechas
    override fun getTotalIncomeBetweenDates(startDate: Long, endDate: Long): Flow<Double?> {
        return transactionDao.getTotalAmountByTypeAndDateRange(TransactionType.INCOME.name, startDate, endDate)
    }

    // Obtener el total de gastos por rango de fechas
    override fun getTotalExpensesBetweenDates(startDate: Long, endDate: Long): Flow<Double?> {
        return transactionDao.getTotalAmountByTypeAndDateRange(TransactionType.EXPENSE.name, startDate, endDate)
    }

    // Obtener gastos por categoría en un rango de fechas
    override fun getExpensesByCategoryInRange(startDate: Long, endDate: Long): Flow<List<ExpenseByCategory>> {
        return transactionDao.getExpensesByCategoryInRange(startDate, endDate)
    }
}