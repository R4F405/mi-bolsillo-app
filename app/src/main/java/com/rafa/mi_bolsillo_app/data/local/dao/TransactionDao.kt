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

/**
 * DAO (Data Access Object) para la entidad [Transaction].
 *
 * Proporciona los métodos que la aplicación utiliza para interactuar con la tabla 'transactions'
 * en la base de datos. Todas las operaciones de base de datos deben ejecutarse fuera del hilo principal,
 * por lo que las funciones que realizan operaciones de escritura o lectura única son `suspend`
 * y las que observan cambios en los datos devuelven [Flow].
 */


@Dao
interface TransactionDao {

    //Insertar una nueva transacción en la base de datos.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    //Actualizar una transacción existente en la base de datos.
    @Update
    suspend fun updateTransaction(transaction: Transaction)

    //Eliminar una transacción existente en la base de datos.
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    //Obtener una transacción por su ID.
    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Long): Transaction?

    //Obtener todas las transacciones.
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    //Obtener todas las transacciones de un tipo.
    @Query("SELECT * FROM transactions WHERE transaction_type = :transactionType ORDER BY date DESC")
    fun getTransactionsByType(transactionType: TransactionType): Flow<List<Transaction>>

    //Obtener todas las transacciones de una categoría.
    @Query("SELECT * FROM transactions WHERE category_id = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategoryId(categoryId: Long): Flow<List<Transaction>>

    //Obtener todas las transacciones en un rango de fechas.
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>

    //Obtener todas las transacciones en un rango de fechas.
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<Transaction>>

    //Obtener el total de una transacción en un rango de fechas.
    @Query("SELECT SUM(amount) FROM transactions WHERE transaction_type = :transactionType AND date BETWEEN :startDate AND :endDate")
    fun getTotalAmountByTypeAndDateRange(transactionType: String, startDate: Long, endDate: Long): Flow<Double?>

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