package com.rafa.mi_bolsillo_app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa un presupuesto mensual para una categoría específica.
 *
 * @property id Identificador único autogenerado para el presupuesto.
 * @property categoryId Clave foránea que referencia al 'id' de la tabla [Category].
 * @property amount Límite de gasto para este presupuesto.
 * @property month El mes del presupuesto (1-12).
 * @property year El año del presupuesto.
 * @property creationDate Timestamp de la fecha de creación.
 */

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["category_id"]),
        Index(value = ["year", "month", "category_id"], unique = true)
    ]
)
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "month")
    val month: Int, // 1-12

    @ColumnInfo(name = "year")
    val year: Int,

    // --- NUEVO CAMPO ---
    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "creation_date", defaultValue = "CURRENT_TIMESTAMP")
    val creationDate: Long = System.currentTimeMillis()
)