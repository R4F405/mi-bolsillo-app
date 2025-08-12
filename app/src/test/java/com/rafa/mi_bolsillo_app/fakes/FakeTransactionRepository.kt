package com.rafa.mi_bolsillo_app.fakes

import com.rafa.mi_bolsillo_app.data.local.dao.ExpenseByCategory
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Una implementación falsa de [TransactionRepository] para fines de prueba.
 * Utiliza un MutableStateFlow para simular la base de datos y
 * permite insertar, actualizar, eliminar y recuperar transacciones.
 */

class FakeTransactionRepository : TransactionRepository {

    val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())
    private var nextId = 1L

    override suspend fun insertTransaction(transaction: Transaction) {
        val currentList = transactionsFlow.value.toMutableList()
        currentList.add(transaction.copy(id = transaction.id.takeIf { it != 0L } ?: nextId++))
        transactionsFlow.value = currentList
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        val currentList = transactionsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            currentList[index] = transaction
            transactionsFlow.value = currentList
        }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionsFlow.value = transactionsFlow.value.filterNot { it.id == transaction.id }
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return transactionsFlow.value.find { it.id == id }
    }

    override fun getAllTransactions(): Flow<List<Transaction>> = transactionsFlow.asStateFlow()

    override suspend fun getAllTransactionsList(): List<Transaction> {
        return transactionsFlow.value
    }

    override fun getTransactionsByType(transactionType: TransactionType): Flow<List<Transaction>> {
        return transactionsFlow.map { list -> list.filter { it.transactionType == transactionType } }
    }

    override fun getTransactionsByCategoryId(categoryId: Long): Flow<List<Transaction>> {
        return transactionsFlow.map { list -> list.filter { it.categoryId == categoryId } }
    }

    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionsFlow.map { list -> list.filter { it.date in startDate..endDate } }
    }

    override fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionsFlow.map { list -> list.filter { it.date in startDate..endDate } }
    }

    override fun getTotalIncomeBetweenDates(startDate: Long, endDate: Long): Flow<Double?> {
        return getTransactionsBetweenDates(startDate, endDate).map { list ->
            list.filter { it.transactionType == TransactionType.INCOME }.sumOf { it.amount }
        }
    }

    override fun getTotalExpensesBetweenDates(startDate: Long, endDate: Long): Flow<Double?> {
        return getTransactionsBetweenDates(startDate, endDate).map { list ->
            list.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amount }
        }
    }

    override fun getExpensesByCategoryInRange(startDate: Long, endDate: Long): Flow<List<ExpenseByCategory>> {
        return getTransactionsBetweenDates(startDate, endDate).map { list ->
            list.filter { it.transactionType == TransactionType.EXPENSE }
                .groupBy { it.categoryId }
                .map { (categoryId, transactions) ->
                    ExpenseByCategory(
                        categoryName = "Category $categoryId", // Simulado
                        categoryColorHex = "#FFFFFF", // Simulado
                        totalAmount = transactions.sumOf { it.amount }
                    )
                }
        }
    }
}