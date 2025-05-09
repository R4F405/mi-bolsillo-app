package com.rafa.mi_bolsillo_app.data.local.converters

import androidx.room.TypeConverter
import com.rafa.mi_bolsillo_app.data.local.entity.TransactionType

/**
 * TypeConverter para que Room pueda almacenar y leer el enum [TransactionType].
 * Convierte el enum a String y viceversa.
 */
class TransactionTypeConverter {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name // Almacena el nombre del enum como String (e.g., "INCOME")
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value) // Convierte el String de vuelta al enum
    }
}