package com.rafa.mi_bolsillo_app.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.repository.RecurringTransactionRepository
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
import com.rafa.mi_bolsillo_app.utils.RecurrenceHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "RecurringTransactionWorker"
        private const val TAG = "RecurringTxWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "RecurringTransactionWorker: Iniciando trabajo...")
        try {
            val currentTime = System.currentTimeMillis()
            val dueTemplates = recurringTransactionRepository.getDueRecurringTransactions(currentTime)

            if (dueTemplates.isEmpty()) {
                Log.d(TAG, "No hay plantillas recurrentes vencidas.")
                return@withContext Result.success()
            }

            Log.d(TAG, "Plantillas encontradas para procesar: ${dueTemplates.size}")

            for (template in dueTemplates) {
                // 1. Generar la transacción
                val newTransaction = Transaction(
                    amount = template.amount,
                    date = template.nextOccurrenceDate, // La fecha de la transacción es la 'nextOccurrenceDate'
                    description = template.description ?: template.name, // Usar nombre de plantilla si no hay descripción
                    categoryId = template.categoryId,
                    transactionType = template.transactionType
                )
                transactionRepository.insertTransaction(newTransaction)
                Log.d(TAG, "Transacción generada para plantilla ID ${template.id}: ${newTransaction.description} - ${newTransaction.amount}")

                // 2. Calcular la siguiente fecha de ocurrencia
                val nextValidOccurrence = RecurrenceHelper.calculateNextOccurrenceDate(
                    lastOccurrence = template.nextOccurrenceDate, // Base para el siguiente cálculo
                    frequency = template.frequency,
                    interval = template.interval,
                    dayOfMonth = template.dayOfMonth,
                    monthOfYear = template.monthOfYear
                )

                // 3. Actualizar la plantilla
                val updatedTemplate = template.copy(
                    lastGeneratedDate = template.nextOccurrenceDate, // La que acabamos de usar
                    nextOccurrenceDate = nextValidOccurrence
                )

                // 4. Verificar si la recurrencia debe desactivarse (si endDate existe y la nueva ocurrencia la supera)
                if (template.endDate != null && nextValidOccurrence > template.endDate) {
                    Log.d(TAG, "Plantilla ID ${template.id} ha alcanzado su fecha de finalización. Desactivando.")
                    recurringTransactionRepository.updateRecurringTransaction(
                        updatedTemplate.copy(isActive = false)
                    )
                } else {
                    recurringTransactionRepository.updateRecurringTransaction(updatedTemplate)
                    Log.d(TAG, "Plantilla ID ${template.id} actualizada. Próxima ocurrencia: $nextValidOccurrence")
                }
            }
            Log.d(TAG, "RecurringTransactionWorker: Trabajo completado exitosamente.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la ejecución de RecurringTransactionWorker", e)
            Result.failure() // O Result.retry() si tiene sentido
        }
    }
}