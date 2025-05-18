package com.rafa.mi_bolsillo_app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter // Importar TypeConverter
import androidx.room.TypeConverters
import com.rafa.mi_bolsillo_app.data.local.converters.TransactionTypeConverter

// Enum para la frecuencia de la recurrencia
enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

// Converter para RecurrenceFrequency, colocarlo aquí o en su propio archivo en el paquete 'converters'
class RecurrenceFrequencyConverter {
    @TypeConverter
    fun fromRecurrenceFrequency(value: RecurrenceFrequency?): String? { // Permitir nulos si es necesario, aunque la entidad no lo permite actualmente
        return value?.name
    }

    @TypeConverter
    fun toRecurrenceFrequency(value: String?): RecurrenceFrequency? { // Permitir nulos si es necesario
        return value?.let { RecurrenceFrequency.valueOf(it) }
    }
}

/**
 * Representa una plantilla para una transacción recurrente.
 * Esta entidad define las reglas para generar transacciones futuras.
 */
@Entity(
    tableName = "recurring_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT // Previene borrar una categoría si tiene plantillas asociadas.
            // Considera ForeignKey.CASCADE si quieres que se borren las plantillas
            // recurrentes al borrar su categoría.
        )
    ],
    indices = [Index(value = ["category_id"])]
)
// Es importante registrar TODOS los TypeConverters que usa esta entidad.
// TransactionTypeConverter ya está registrado a nivel de base de datos,
// pero si no lo estuviera, habría que añadirlo aquí también.
@TypeConverters(RecurrenceFrequencyConverter::class)
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name") // Nombre para identificar la recurrencia (ej. "Hipoteca", "Suscripción Netflix")
    val name: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    @ColumnInfo(name = "transaction_type")
    val transactionType: TransactionType, // Usará el TransactionTypeConverter global

    @ColumnInfo(name = "start_date")
    val startDate: Long, // Timestamp de la fecha en que la recurrencia comienza a ser válida

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
    val creationDate: Long = System.currentTimeMillis() // Fecha de creación de la plantilla
)