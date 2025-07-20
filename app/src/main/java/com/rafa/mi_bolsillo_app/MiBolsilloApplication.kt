package com.rafa.mi_bolsillo_app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rafa.mi_bolsillo_app.workers.RecurringTransactionWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Clase Application personalizada para MiBolsillo.
 * Configura Hilt para la inyección de dependencias y WorkManager para manejar trabajos periódicos.
 * Esta configuración permite que la aplicación realice tareas en segundo plano, como procesar transacciones recurrentes,
 * sin necesidad de que la aplicación esté abierta.
 *
 */

@HiltAndroidApp
class MiBolsilloApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        setupRecurringWork()
    }

    // Configuración para que Hilt pueda inyectar dependencias en Workers
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG) // Ver logs de WorkManager
            .build()

    /*
    Crear una solicitud de trabajo periódica.
    Repetir cada 24 horas.
    WorkManager intentará ejecutarlo alrededor de este tiempo, teniendo en cuenta restricciones del sistema como Doze mode.
    */
    private fun setupRecurringWork() {
        val workManager = WorkManager.getInstance(applicationContext)
        val recurringTxRequest =
            PeriodicWorkRequestBuilder<RecurringTransactionWorker>(24, TimeUnit.HOURS)
                .build()

        // Encolar el trabajo periódico, manteniendo el trabajo existente si ya está programado
        workManager.enqueueUniquePeriodicWork(
            RecurringTransactionWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // KEEP: Si ya existe un trabajo con este nombre, no hacer nada.
            // REPLACE: Cancela y reemplaza el existente.
            recurringTxRequest
        )
    }
}