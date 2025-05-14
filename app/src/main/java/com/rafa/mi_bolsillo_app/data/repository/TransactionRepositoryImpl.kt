package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.data.local.dao.ExpenseByCategory
import com.rafa.mi_bolsillo_app.data.local.dao.TransactionDao
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de transacciones.
 *
 * Proporciona métodos para interactuar con la tabla de transacciones en la base de datos.
 *
 */

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions() // Obtener todas las transacciones
    override fun getTransactionsByType(transactionType: TransactionType): Flow<List<Transaction>> = transactionDao.getTransactionsByType(transactionType) // Obtener transacciones por tipo
    override fun getTransactionsByCategoryId(categoryId: Long): Flow<List<Transaction>> = transactionDao.getTransactionsByCategoryId(categoryId) // Obtener transacciones por categoría
    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> = transactionDao.getTransactionsByDateRange(startDate, endDate) // Obtener transacciones por rango de fechas
    override suspend fun getTransactionById(id: Long): Transaction? = transactionDao.getTransactionById(id) // Obtener una transacción por su ID
    override suspend fun insertTransaction(transaction: Transaction) = transactionDao.insertTransaction(transaction) // Insertar una nueva transacción
    override suspend fun updateTransaction(transaction: Transaction) = transactionDao.updateTransaction(transaction) // Actualizar una transacción
    override suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction) // Eliminar una transacción

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