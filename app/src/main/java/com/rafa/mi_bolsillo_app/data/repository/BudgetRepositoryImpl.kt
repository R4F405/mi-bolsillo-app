package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.data.local.dao.BudgetDao
import com.rafa.mi_bolsillo_app.data.local.entity.Budget
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de presupuestos.
 * Proporciona acceso a los datos de presupuestos a través de la capa de persistencia.
 * Utiliza el DAO de presupuestos para realizar operaciones CRUD y consultas específicas.
 */

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getBudgetsForMonth(year: Int, month: Int): Flow<List<Budget>> =
        budgetDao.getBudgetsForMonth(year, month)

    override suspend fun getBudgetForCategory(year: Int, month: Int, categoryId: Long): Budget? =
        budgetDao.getBudgetForCategory(year, month, categoryId)

    override suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }

    override suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }

    override suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    override fun getFavoriteBudgets(): Flow<List<Budget>> = budgetDao.getFavoriteBudgets()

    override suspend fun updateFavoriteStatus(budgetId: Long, isFavorite: Boolean) {
        budgetDao.updateFavoriteStatus(budgetId, isFavorite)
    }
}