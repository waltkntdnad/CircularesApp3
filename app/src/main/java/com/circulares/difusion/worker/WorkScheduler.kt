package com.circulares.difusion.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.circulares.difusion.util.Config
import java.util.concurrent.TimeUnit

/**
 * Programa (de forma idempotente) el trabajo periódico que reactiva la
 * visualización de circulares cada 4 horas.
 */
object WorkScheduler {

    private const val NOMBRE_TRABAJO = "difusion_circulares_periodica"

    fun scheduleRecurringDisplay(context: Context) {
        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val solicitud = PeriodicWorkRequestBuilder<CircularDisplayWorker>(
            Config.INTERVALO_HORAS, TimeUnit.HOURS
        )
            .setConstraints(restricciones)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NOMBRE_TRABAJO,
            // KEEP: si ya está programado, no lo dupliques.
            ExistingPeriodicWorkPolicy.KEEP,
            solicitud
        )
    }
}
