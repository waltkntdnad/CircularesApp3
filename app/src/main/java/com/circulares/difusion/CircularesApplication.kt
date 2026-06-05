package com.circulares.difusion

import android.app.Application
import android.util.Log
import com.circulares.difusion.notifications.NotificationHelper
import com.circulares.difusion.worker.WorkScheduler
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Clase Application principal.
 * Se inicializa una sola vez al arrancar la app en cualquier dispositivo.
 * Aquí:
 *  - Creamos el canal de notificaciones (requerido en Android 8.0+).
 *  - Programamos el trabajo periódico de WorkManager (repetición cada 4 h).
 *  - Suscribimos el dispositivo al topic "circulares" de FCM para recibir
 *    el push instantáneo que envía la Cloud Function al publicar una circular,
 *    incluso con la app cerrada.
 */
class CircularesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Canal de notificaciones (Android 8.0+)
        NotificationHelper(this).createNotificationChannel()

        // WorkManager: repetición de visualización cada 4 h
        WorkScheduler.scheduleRecurringDisplay(this)

        // Suscripción al topic de FCM (idempotente: se puede llamar siempre)
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_CIRCULARES)
            .addOnCompleteListener { tarea ->
                val msg = if (tarea.isSuccessful) {
                    "Suscrito al topic '$TOPIC_CIRCULARES'"
                } else {
                    "No se pudo suscribir al topic '$TOPIC_CIRCULARES'"
                }
                Log.d("FCM", msg)
            }
    }

    companion object {
        const val TOPIC_CIRCULARES = "circulares"
    }
}
