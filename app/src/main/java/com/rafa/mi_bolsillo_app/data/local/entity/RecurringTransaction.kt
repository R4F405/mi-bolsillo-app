package com.rafa.mi_bolsillo_app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.rafa.mi_bolsillo_app.data.local.converters.TransactionTypeConverter

// Enum para la frecuencia de la recurrencia
enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * TypeConverter para que Room pueda almacenar y leer el enum [RecurrenceFrequency].
 * Convierte el enum a String y viceversa.
 */

class RecurrenceFrequencyConverter {
    @TypeConverter
    fun fromRecurrenceFrequency(value: RecurrenceFrequency?): String? {
        return value?.name
    }

    @TypeConverter
    fun toRecurrenceFrequency(value: String?): RecurrenceFrequency? {
        return value?.let { RecurrenceFrequency.valueOf(it) }
    }
}

/**
 * Representa una transacción recurrente que se generará automáticamente
 *
 * @property id Identificador único autogenerado para la transacción recurrente.
 * @property name Nombre para identificar la recurrencia (ej. "Hipoteca", "Suscripción Netflix").
 * @property amount Monto de la transacción recurrente.
 * @property description Descripción opcional de la transacción recurrente.
 * @property categoryId Clave foránea que referencia al 'id' de la tabla [Category].
 * @property transactionType Tipo de transacción (ingreso o gasto), usando el [TransactionTypeConverter] global.
 * @property startDate Timestamp de la fecha en que la recurrencia comienza a ser válida.
 * @property frequency Frecuencia de la recurrencia (diaria, semanal, mensual, anual).
 * @property interval Intervalo de la recurrencia (ej. cada 1 mes, cada 2 semanas).
 * @property dayOfMonth Día del mes específico para recurrencias mensuales (1-31) o día de la semana (1-7 Calendar.DAY_OF_WEEK) para semanales.
 * @property monthOfYear Mes del año específico para recurrencias anuales (0-11 Calendar.MONTH).
 * @property endDate Timestamp de cuándo debe finalizar la recurrencia (opcional).
 * @property nextOccurrenceDate Próxima fecha en la que se debe generar una transacción. Se actualiza después de cada generación.
 * @property lastGeneratedDate Fecha de la última instancia generada (opcional).
 * @property isActive Indica si la plantilla está activa o no. Permite activar/desactivar
 * @property creationDate Fecha de creación de la plantilla, por defecto es el timestamp actual.
 */

@Entity(
    tableName = "recurring_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT // Previene borrar una categoría si tiene plantillas asociadas.
            // ForeignKey.CASCADE para que se borren las plantillas
        )
    ],
    indices = [Index(value = ["category_id"])]
)

@TypeConverters(RecurrenceFrequencyConverter::class)
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    @ColumnInfo(name = "transaction_type")
    val transactionType: TransactionType, // Usará el TransactionTypeConverter global, tipo de transacción (ingreso o gasto)

    @ColumnInfo(name = "start_date")
    val startDate: Long,

    @ColumnInfo(name = "frequency")
    val frequency: RecurrenceFrequency,

    @ColumnInfo(name = "interval")
    val interval: Int = 1, // Ej: frequency=MONTHLY, interval=1 -> cada mes. frequency=WEEKLY, interval=2 -> cada 2 semanas.

    @ColumnInfo(name = "day_of_month") // Para recurrencias mensuales específicas (1-31) o DAY_OF_WEEK para semanales (1-7 Calendar.DAY_OF_WEEK)
    val dayOfMonth: Int? = null, // Si es mensual y se quiere un día específico. Si es null, se basa en el día de startDate.

    @ColumnInfo(name = "month_of_year") // Para recurrencias anuales específicas (0-11 Calendar.MONTH)
    val monthOfYear: Int? = null, // Si es anual y se quiere un mes específico. Si es null, se basa en el mes de startDate.

    @ColumnInfo(name = "end_date")
    val endDate: Long?, // Timestamp de cuándo debe finalizar la recurrencia (opcional)

    @ColumnInfo(name = "next_occurrence_date")
    var nextOccurrenceDate: Long, // Próxima fecha en la que se debe generar una transacción. Se actualiza después de cada generación.

    @ColumnInfo(name = "last_generated_date") // Opcional: para saber cuándo se generó la última instancia
    var lastGeneratedDate: Long? = null,

    @ColumnInfo(name = "is_active")
    var isActive: Boolean = true, // Para activar/desactivar la plantilla

    @ColumnInfo(name = "creation_date", defaultValue = "CURRENT_TIMESTAMP")
    val creationDate: Long = System.currentTimeMillis()
)