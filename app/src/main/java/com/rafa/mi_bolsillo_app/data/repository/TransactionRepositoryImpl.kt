package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.data.local.dao.ExpenseByCategory
import com.rafa.mi_bolsillo_app.data.local.dao.TransactionDao
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    override fun getTransactionsByType(transactionType: TransactionType): Flow<List<Transaction>> =
        transactionDao.getTransactionsByType(transactionType)
    override fun getTransactionsByCategoryId(categoryId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCategoryId(categoryId)
    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(startDate, endDate)
    override suspend fun getTransactionById(id: Long): Transaction? = transactionDao.getTransactionById(id)
    override suspend fun insertTransaction(transaction: Transaction) = transactionDao.insertTransaction(transaction)
    override suspend fun updateTransaction(transaction: Transaction) = transactionDao.updateTransaction(transaction)
    override suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction)

    override fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate)
    }

    override fun getTotalIncomeBetweenDates(startDate: Long, endDate: Long): Flow<Double?> {
        return transactionDao.getTotalAmountByTypeAndDateRange(TransactionType.INCOME.name, startDate, endDate)
    }

    override fun getTotalExpensesBetweenDates(startDate: Long, endDate: Long): Flow<Double?> {
        return transactionDao.getTotalAmountByTypeAndDateRange(TransactionType.EXPENSE.name, startDate, endDate)
    }

    override fun getExpensesByCategoryInRange(startDate: Long, endDate: Long): Flow<List<ExpenseByCategory>> {
        return transactionDao.getExpensesByCategoryInRange(startDate, endDate)
    }
}