package com.rafa.mi_bolsillo_app.utils

import com.rafa.mi_bolsillo_app.data.local.entity.RecurrenceFrequency
import java.util.Calendar
import java.util.TimeZone

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
        // al comparar solo fechas. Esto es importante si las transacciones se generan "al inicio del día".
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
                // Si se quisiera un día específico de la semana, se ajustaría aquí.
                // Por ahora, se basa en el día de la semana de 'lastOccurrence'.
            }
            RecurrenceFrequency.MONTHLY -> {
                // Primero, avanzar el mes
                calendar.add(Calendar.MONTH, interval)

                // Si se especifica un día del mes, intentar ajustarlo
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