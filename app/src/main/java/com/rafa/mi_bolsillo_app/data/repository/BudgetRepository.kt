package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.data.local.entity.Budget
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define las operaciones de acceso a datos para los presupuestos.
 * Proporciona métodos para obtener, insertar, actualizar y eliminar presupuestos,
 * así como para manejar presupuestos favoritos.
 */

interface BudgetRepository {
    fun getBudgetsForMonth(year: Int, month: Int): Flow<List<Budget>>
    suspend fun getBudgetForCategory(year: Int, month: Int, categoryId: Long): Budget?
    suspend fun insertBudget(budget: Budget)
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudget(budget: Budget)
    fun getFavoriteBudgets(): Flow<List<Budget>>
    suspend fun updateFavoriteStatus(budgetId: Long, isFavorite: Boolean)
}