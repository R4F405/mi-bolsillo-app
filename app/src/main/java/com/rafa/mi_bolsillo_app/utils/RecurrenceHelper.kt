package com.rafa.mi_bolsillo_app.utils

import com.rafa.mi_bolsillo_app.data.local.entity.RecurrenceFrequency
import java.util.Calendar
import java.util.TimeZone

/**
 * Helper para calcular la próxima fecha de ocurrencia de una transacción recurrente.
 * Utiliza la frecuencia, intervalo y opcionalmente el día del mes y mes del año.
 *
 * @param lastOccurrence La fecha base para el cálculo (startDate o la última nextOccurrenceDate).
 * @param frequency La frecuencia de recurrencia (DIARIA, SEMANAL, MENSUAL, ANUAL).
 * @param interval El intervalo de recurrencia (1 para cada unidad de tiempo, 2 para cada dos unidades, etc.).
 * @param dayOfMonth Opcional: el día del mes (1-31) para MENSUAL y ANUAL.
 * @param monthOfYear Opcional: el mes del año (0-11) para ANUAL.
 * @return La próxima fecha de ocurrencia en milisegundos desde epoch.
 */

object RecurrenceHelper {

    fun calculateNextOccurrenceDate(
        lastOccurrence: Long, // La fecha base para el cálculo (startDate o la última nextOccurrenceDate)
        frequency: RecurrenceFrequency,
        interval: Int,
        dayOfMonth: Int?, // 1-31 (para MONTHLY y YEARLY)
        monthOfYear: Int? // 0-11 (para YEARLY, de Calendar.JANUARY a Calendar.DECEMBER)
    ): Long {
        val calendar = Calendar.getInstance(TimeZone.getDefault()) // Usar TimeZone por defecto
        calendar.timeInMillis = lastOccurrence

        // Normalizar la hora a medianoche para evitar problemas con cambios de hora o DST
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        when (frequency) {
            RecurrenceFrequency.DAILY -> {
                calendar.add(Calendar.DAY_OF_YEAR, interval)
            }
            RecurrenceFrequency.WEEKLY -> {
                calendar.add(Calendar.WEEK_OF_YEAR, interval)
            }
            RecurrenceFrequency.MONTHLY -> {
                calendar.add(Calendar.MONTH, interval)

                // Si se especifica un día del mes, lo ajusta
                dayOfMonth?.let { targetDay ->
                    val currentMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    if (targetDay > currentMaxDay) {
                        calendar.set(Calendar.DAY_OF_MONTH, currentMaxDay) // Ajustar al último día si el target es mayor
                    } else {
                        calendar.set(Calendar.DAY_OF_MONTH, targetDay)
                    }
                }
                // Si dayOfMonth es null, se mantiene el día actual del mes (o el último día si el mes es más corto).
                // Calendar.add(MONTH) ya maneja esto de forma inteligente (ej. Ene 31 + 1 mes = Feb 28/29)
            }
            RecurrenceFrequency.YEARLY -> {
                calendar.add(Calendar.YEAR, interval)

                monthOfYear?.let { targetMonth ->
                    calendar.set(Calendar.MONTH, targetMonth)
                }
                dayOfMonth?.let { targetDay ->
                    val currentMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    if (targetDay > currentMaxDay) {
                        calendar.set(Calendar.DAY_OF_MONTH, currentMaxDay)
                    } else {
                        calendar.set(Calendar.DAY_OF_MONTH, targetDay)
                    }
                }
                // Si son null, se mantiene el mes/día original
            }
        }
        return calendar.timeInMillis
    }
}