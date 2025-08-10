package com.rafa.mi_bolsillo_app.fakes

import com.rafa.mi_bolsillo_app.data.local.entity.Budget
import com.rafa.mi_bolsillo_app.data.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Una implementación falsa de [BudgetRepository] para fines de prueba.
 * Utiliza un MutableStateFlow para simular la base de datos y
 * permite insertar, actualizar, eliminar y recuperar presupuestos.
 */

class FakeBudgetRepository : BudgetRepository {
    val budgetsFlow = MutableStateFlow<List<Budget>>(emptyList())

    fun addBudget(budget: Budget) {
        budgetsFlow.value += budget
    }

    override fun getBudgetsForMonth(year: Int, month: Int): Flow<List<Budget>> = flowOf(
        budgetsFlow.value.filter { it.year == year && it.month == month }
    )

    override suspend fun getBudgetForCategory(year: Int, month: Int, categoryId: Long): Budget? {
        return budgetsFlow.value.find { it.year == year && it.month == month && it.categoryId == categoryId }
    }

    override suspend fun insertBudget(budget: Budget) {
        budgetsFlow.value += budget.copy(id = (budgetsFlow.value.maxOfOrNull { it.id } ?: 0L) + 1)
    }

    override suspend fun updateBudget(budget: Budget) {
        val currentList = budgetsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == budget.id }
        if (index != -1) {
            currentList[index] = budget
            budgetsFlow.value = currentList
        }
    }

    override suspend fun deleteBudget(budget: Budget) {
        budgetsFlow.value = budgetsFlow.value.filterNot { it.id == budget.id }
    }

    override fun getFavoriteBudgets(): Flow<List<Budget>> = flowOf(budgetsFlow.value.filter { it.isFavorite })

    override suspend fun updateFavoriteStatus(budgetId: Long, isFavorite: Boolean) {
        val currentList = budgetsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == budgetId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isFavorite = isFavorite)
            budgetsFlow.value = currentList
        }
    }
}