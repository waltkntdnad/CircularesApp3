package com.circulares.difusion.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.circulares.difusion.data.repository.CircularRepository
import com.circulares.difusion.notifications.NotificationHelper

/**
 * Worker que se ejecuta periódicamente (cada 4 horas).
 *
 * En cada ejecución:
 *  1. Actualiza el estado de las circulares (marca como expiradas las que
 *     superaron los 2 días).
 *  2. Obtiene las circulares aún vigentes.
 *  3. Si hay alguna vigente, emite una notificación que reactiva la
 *     visualización en el dispositivo.
 *
 * El propio sistema garantiza la repetición cada 4 h; el periodo de 2 días se
 * controla con la fecha de expiración: pasado ese tiempo no hay circulares
 * vigentes y, por tanto, no se emite notificación.
 */
class CircularDisplayWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val repository = CircularRepository()

    override suspend fun doWork(): Result {
        return try {
            repository.actualizarEstados()
            val vigentes = repository.obtenerCircularesVigentes()

            if (vigentes.isNotEmpty()) {
                val helper = NotificationHelper(applicationContext)
                val titulo = if (vigentes.size == 1) {
                    "Circular vigente"
                } else {
                    "${vigentes.size} circulares vigentes"
                }
                val mensaje = vigentes.firstOrNull()?.titulo
                    ?.let { "Recordatorio: \"$it\". Toca para visualizar." }
                    ?: "Tienes circulares pendientes por visualizar."
                helper.mostrarNotificacion(titulo, mensaje, NOTIF_ID)
            }
            Result.success()
        } catch (e: Exception) {
            // Reintenta más tarde si hubo un fallo de red.
            Result.retry()
        }
    }

    companion object {
        private const val NOTIF_ID = 1001
    }
}
