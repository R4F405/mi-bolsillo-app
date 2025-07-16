package com.rafa.mi_bolsillo_app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa una categoría para las transacciones en la base de datos.
 *
 * @property id Identificador único autogenerado para la categoría.
 * @property name Nombre de la categoría (ej. "Comida", "Transporte"). Debe ser único.
 * @property colorHex Código hexadecimal del color asociado a la categoría (ej. "#FF5733").
 * @property isPredefined Indica si la categoría es una de las predefinidas por la app (true) o creada por el usuario (false).
 */
@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)] // Asegura que los nombres de categoría sean únicos
)

data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color_hex")
    val colorHex: String, // Ejemplo: "#4CAF50" para verde, "#F44336" para rojo

    @ColumnInfo(name = "is_predefined", defaultValue = "0")
    val isPredefined: Boolean = false
)