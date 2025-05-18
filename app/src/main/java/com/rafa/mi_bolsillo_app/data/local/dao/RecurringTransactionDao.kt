package com.rafa.mi_bolsillo_app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rafa.mi_bolsillo_app.data.local.entity.RecurringTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction): Long

    @Update
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction)

    @Delete
    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction)

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getRecurringTransactionById(id: Long): RecurringTransaction?

    @Query("SELECT * FROM recurring_transactions ORDER BY start_date DESC")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>

    // Obtener plantillas activas que están listas para generar una transacción
    // y cuya próxima ocurrencia no haya superado la fecha de finalización (si existe)
    @Query("SELECT * FROM recurring_transactions WHERE is_active = 1 AND next_occurrence_date <= :currentDate AND (end_date IS NULL OR next_occurrence_date <= end_date)")
    suspend fun getDueRecurringTransactions(currentDate: Long): List<RecurringTransaction>

    @Query("SELECT * FROM recurring_transactions WHERE is_active = 1 ORDER BY next_occurrence_date ASC")
    fun getActiveRecurringTransactionsSortedByNextOccurrence(): Flow<List<RecurringTransaction>>
}