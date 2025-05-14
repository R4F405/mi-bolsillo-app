package com.rafa.mi_bolsillo_app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.rafa.mi_bolsillo_app.data.local.converters.TransactionTypeConverter

/**
 * Representa una transacción financiera (ingreso o gasto) en la base de datos.
 *
 * @property id Identificador único autogenerado para la transacción.
 * @property amount Monto de la transacción. Positivo para ingresos, usualmente positivo para gastos también,
 * el tipo [transactionType] define su naturaleza.
 * @property date Fecha en la que se realizó la transacción, almacenada como timestamp (milisegundos desde la época).
 * @property description Descripción opcional de la transacción.
 * @property categoryId Clave foránea que referencia al 'id' de la tabla [Category].
 * Indica a qué categoría pertenece esta transacción.
 * @property transactionType Tipo de transacción, si es un [TransactionType.INCOME] o [TransactionType.EXPENSE].
 * Se almacena como String gracias al [TransactionTypeConverter].
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT // Previene borrar una categoría si tiene transacciones asociadas.
        )
    ],
    indices = [Index(value = ["category_id"])] // Index para optimizar búsquedas por category_id
)
@TypeConverters(TransactionTypeConverter::class) // Registra el converter para esta entidad
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "date")
    val date: Long,

    @ColumnInfo(name = "description")
    val description: String?, // Puede ser nulo si no se proporciona descripción

    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    @ColumnInfo(name = "transaction_type")
    val transactionType: TransactionType
)