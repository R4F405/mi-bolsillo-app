package com.rafa.mi_bolsillo_app.utils

import com.rafa.mi_bolsillo_app.data.local.entity.RecurrenceFrequency
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * Tests unitarios para la clase RecurrenceHelper.
 *
 * Estos tests verifican que el cálculo de la próxima fecha de ocurrencia
 * funciona correctamente para todas las frecuencias y casos de uso.
 */
class RecurrenceHelperTest {

    // Configura una fecha inicial para todos los tests: 15 de Enero de 2025 a las 10:00 AM
    private fun getInitialCalendar(): Calendar {
        // Usar una zona horaria consistente para los tests para evitar problemas
        val tz = TimeZone.getDefault()
        return Calendar.getInstance(tz).apply {
            set(2025, Calendar.JANUARY, 15, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    @Test
    fun `calculateNextOccurrenceDate con frecuencia DIARIA y intervalo 1`() {
        val initialDate = getInitialCalendar()
        val expectedDate = getInitialCalendar().apply { add(Calendar.DAY_OF_YEAR, 1) }

        val nextDate = RecurrenceHelper.calculateNextOccurrenceDate(
            lastOccurrence = initialDate.timeInMillis,
            frequency = RecurrenceFrequency.DAILY,
            interval = 1,
            dayOfMonth = null,
            monthOfYear = null
        )

        // El resultado debe ser el día siguiente a la misma hora (normalizado a medianoche)
        expectedDate.set(Calendar.HOUR_OF_DAY, 0) // El helper normaliza la hora
        assertEquals(expectedDate.timeInMillis, nextDate)
    }

    @Test
    fun `calculateNextOccurrenceDate con frecuencia DIARIA y intervalo 5`() {
        val initialDate = getInitialCalendar()
        val expectedDate = getInitialCalendar().apply { add(Calendar.DAY_OF_YEAR, 5) }

        val nextDate = RecurrenceHelper.calculateNextOccurrenceDate(
            lastOccurrence = initialDate.timeInMillis,
            frequency = RecurrenceFrequency.DAILY,
            interval = 5,
            dayOfMonth = null,
            monthOfYear = null
        )

        expectedDate.set(Calendar.HOUR_OF_DAY, 0)
        assertEquals(expectedDate.timeInMillis, nextDate)
    }

    @Test
    fun `calculateNextOccurrenceDate con frecuencia SEMANAL y intervalo 1`() {
        val initialDate = getInitialCalendar()
        val expectedDate = getInitialCalendar().apply { add(Calendar.WEEK_OF_YEAR, 1) }

        val nextDate = RecurrenceHelper.calculateNextOccurrenceDate(
            lastOccurrence = initialDate.timeInMillis,
            frequency = RecurrenceFrequency.WEEKLY,
            interval = 1,
            dayOfMonth = null,
            monthOfYear = null
        )

        expectedDate.set(Calendar.HOUR_OF_DAY, 0)
        assertEquals(expectedDate.timeInMillis, nextDate)
    }

    @Test
    fun `calculateNextOccurrenceDate con frecuencia MENSUAL y intervalo 1`() {
        val initialDate = getInitialCalendar() // 15 de Enero
        val expectedDate = getInitialCalendar().apply { add(Calendar.MONTH, 1) } // 15 de Febrero

        val nextDate = RecurrenceHelper.calculateNextOccurrenceDate(
            lastOccurrence = initialDate.timeInMillis,
            frequency = RecurrenceFrequency.MONTHLY,
            interval = 1,
            dayOfMonth = null,
            monthOfYear = null
        )

        expectedDate.set(Calendar.HOUR_OF_DAY, 0)
        assertEquals(expectedDate.timeInMillis, nextDate)
    }

    @Test
    fun `calculateNextOccurrenceDate con frecuencia MENSUAL y día específico`() {
        val initialDate = getInitialCalendar() // 15 de Enero
        val expectedDate = getInitialCalendar().apply {
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 20) // Se espera el 20 de Febrero
        }

        val nextDate = RecurrenceHelper.calculateNextOccurrenceDate(
            lastOccurrence = initialDate.timeInMillis,
            frequency = RecurrenceFrequency.MONTHLY,
            interval = 1,
            dayOfMonth = 20, // Queremos que sea el día 20 de cada mes
            monthOfYear = null
        )

        expectedDate.set(Calendar.HOUR_OF_DAY, 0)
        assertEquals(expectedDate.timeInMillis, nextDate)
    }

    @Test
    fun `calculateNextOccurrenceDate con frecuencia MENSUAL y día 31 (salto de mes corto)`() {
        val initialDate = getInitialCalendar().apply { set(2025, Calendar.JANUARY, 31) } // 31 de Enero
        val expectedDate = getInitialCalendar().apply {
            set(2025, Calendar.FEBRUARY, 28) // Febrero de 2025 tiene 28 días
        }

        val nextDate = RecurrenceHelper.calculateNextOccurrenceDate(
            lastOccurrence = initialDate.timeInMillis,
            frequency = RecurrenceFrequency.MONTHLY,
            interval = 1,
            dayOfMonth = 31, // Se ajustará al último día del mes si no existe
            monthOfYear = null
        )

        expectedDate.set(Calendar.HOUR_OF_DAY, 0)
        assertEquals(expectedDate.timeInMillis, nextDate)
    }

    @Test
    fun `calculateNextOccurrenceDate con frecuencia ANUAL y intervalo 2`() {
        val initialDate = getInitialCalendar() // 15 de Enero de 2025
        val expectedDate = getInitialCalendar().apply { add(Calendar.YEAR, 2) } // 15 de Enero de 2027

        val nextDate = RecurrenceHelper.calculateNextOccurrenceDate(
            lastOccurrence = initialDate.timeInMillis,
            frequency = RecurrenceFrequency.YEARLY,
            interval = 2,
            dayOfMonth = null,
            monthOfYear = null
        )

        expectedDate.set(Calendar.HOUR_OF_DAY, 0)
        assertEquals(expectedDate.timeInMillis, nextDate)
    }

    @Test
    fun `calculateNextOccurrenceDate con frecuencia ANUAL y mes y día específicos`() {
        val initialDate = getInitialCalendar() // 15 de Enero de 2025
        val expectedDate = getInitialCalendar().apply {
            add(Calendar.YEAR, 1)
            set(Calendar.MONTH, Calendar.JUNE) // Junio
            set(Calendar.DAY_OF_MONTH, 1)     // Día 1
        }

        val nextDate = RecurrenceHelper.calculateNextOccurrenceDate(
            lastOccurrence = initialDate.timeInMillis,
            frequency = RecurrenceFrequency.YEARLY,
            interval = 1,
            dayOfMonth = 1,
            monthOfYear = Calendar.JUNE
        )

        expectedDate.set(Calendar.HOUR_OF_DAY, 0)
        assertEquals(expectedDate.timeInMillis, nextDate)
    }
}
