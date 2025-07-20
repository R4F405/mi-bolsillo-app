package com.rafa.mi_bolsillo_app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rafa.mi_bolsillo_app.data.local.entity.Budget
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para manejar las operaciones de la tabla de presupuestos.
 * Contiene métodos para insertar, actualizar, eliminar y consultar presupuestos.
 */
@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget): Long

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    suspend fun getBudgetById(budgetId: Long): Budget?

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month")
    fun getBudgetsForMonth(year: Int, month: Int): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month AND category_id = :categoryId")
    suspend fun getBudgetForCategory(year: Int, month: Int, categoryId: Long): Budget?

    @Query("SELECT * FROM budgets WHERE is_favorite = 1")
    fun getFavoriteBudgets(): Flow<List<Budget>>

    @Query("UPDATE budgets SET is_favorite = :isFavorite WHERE id = :budgetId")
    suspend fun updateFavoriteStatus(budgetId: Long, isFavorite: Boolean)
}