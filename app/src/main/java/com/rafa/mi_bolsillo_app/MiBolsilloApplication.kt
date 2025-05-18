package com.rafa.mi_bolsillo_app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory // Necesario para Hilt con WorkManager
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rafa.mi_bolsillo_app.workers.RecurringTransactionWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

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
            .setMinimumLoggingLevel(android.util.Log.DEBUG) // Opcional: para ver logs de WorkManager
            .build()

    private fun setupRecurringWork() {
        val workManager = WorkManager.getInstance(applicationContext)

        // Crear una solicitud de trabajo periódica
        // Repetir cada 24 horas. Puedes ajustar el intervalo.
        // WorkManager intentará ejecutarlo alrededor de este tiempo, teniendo en cuenta
        // restricciones del sistema como Doze mode.
        val recurringTxRequest =
            PeriodicWorkRequestBuilder<RecurringTransactionWorker>(24, TimeUnit.HOURS)
                // Podrías añadir Constraints aquí si es necesario (ej. conectado a red, batería no baja)
                // .setConstraints(Constraints.Builder()...build())
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