package com.rafa.mi_bolsillo_app.fakes

import com.rafa.mi_bolsillo_app.data.local.entity.RecurringTransaction
import com.rafa.mi_bolsillo_app.data.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeRecurringTransactionRepository: RecurringTransactionRepository {
    val templatesFlow = MutableStateFlow<List<RecurringTransaction>>(emptyList())
    private var nextId = 1L

    override fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>> = templatesFlow.asStateFlow()

    override fun getActiveRecurringTransactionsSortedByNextOccurrence(): Flow<List<RecurringTransaction>> {
        return templatesFlow.map { list ->
            list.filter { it.isActive }.sortedBy { it.nextOccurrenceDate }
        }
    }

    override suspend fun getRecurringTransactionById(id: Long): RecurringTransaction? {
        return templatesFlow.value.find { it.id == id }
    }

    override suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction): Long {
        val newTemplate = recurringTransaction.copy(id = recurringTransaction.id.takeIf { it != 0L } ?: nextId++)
        templatesFlow.value += newTemplate
        return newTemplate.id
    }

    override suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        val currentList = templatesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == recurringTransaction.id }
        if (index != -1) {
            currentList[index] = recurringTransaction
            templatesFlow.value = currentList
        }
    }

    override suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) {
        templatesFlow.value = templatesFlow.value.filterNot { it.id == recurringTransaction.id }
    }

    override suspend fun getDueRecurringTransactions(currentDate: Long): List<RecurringTransaction> {
        return templatesFlow.value.filter {
            it.isActive && it.nextOccurrenceDate <= currentDate && (it.endDate == null || it.nextOccurrenceDate <= it.endDate!!)
        }
    }
}