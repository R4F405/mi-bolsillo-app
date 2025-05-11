package com.rafa.mi_bolsillo_app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Long): Transaction?

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE transaction_type = :transactionType ORDER BY date DESC")
    fun getTransactionsByType(transactionType: TransactionType): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE category_id = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategoryId(categoryId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<Transaction>> // Ya lo tenías como getTransactionsByDateRange

    @Query("SELECT SUM(amount) FROM transactions WHERE transaction_type = :transactionType AND date BETWEEN :startDate AND :endDate")
    fun getTotalAmountByTypeAndDateRange(transactionType: String, startDate: Long, endDate: Long): Flow<Double?> // El tipo se guarda como String

    // Para el gráfico: Gastos agrupados por categoría en un rango de fechas
    @Query("""
        SELECT c.name as categoryName, c.color_hex as categoryColorHex, SUM(t.amount) as totalAmount
        FROM transactions t
        INNER JOIN categories c ON t.category_id = c.id
        WHERE t.transaction_type = 'EXPENSE' AND t.date BETWEEN :startDate AND :endDate
        GROUP BY t.category_id, c.name, c.color_hex
        HAVING SUM(t.amount) > 0
        ORDER BY totalAmount DESC
    """)
    fun getExpensesByCategoryInRange(startDate: Long, endDate: Long): Flow<List<ExpenseByCategory>>
}

// Data class para el resultado de la consulta de gastos por categoría
data class ExpenseByCategory(
    val categoryName: String,
    val categoryColorHex: String,
    val totalAmount: Double
)