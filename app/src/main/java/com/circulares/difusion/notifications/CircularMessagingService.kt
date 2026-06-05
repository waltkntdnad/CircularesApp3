package com.circulares.difusion.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Servicio de Firebase Cloud Messaging (FCM).
 *
 * Recibe el push instantáneo que envía la Cloud Function `notificarCircular`
 * al topic "circulares" cada vez que el administrador publica una circular.
 *
 * Comportamiento según el estado de la app:
 *  - App en primer plano: SIEMPRE entra por onMessageReceived (mostramos la
 *    notificación manualmente).
 *  - App en segundo plano o cerrada: si el mensaje trae bloque "notification",
 *    el sistema la muestra automáticamente en la bandeja. Si además trae datos,
 *    los leemos al abrir.
 */
class CircularMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Preferimos el bloque notification; si no, usamos los datos.
        val titulo = message.notification?.title
            ?: message.data["titulo"]
            ?: "Nueva circular"
        val cuerpo = message.notification?.body
            ?: message.data["descripcion"]
            ?: "Hay una nueva circular o instructivo disponible."

        NotificationHelper(this).mostrarNotificacion(
            titulo, cuerpo, System.currentTimeMillis().toInt()
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // El topic "circulares" se gestiona por suscripción (no por token),
        // por lo que no necesitamos registrar el token en el servidor.
    }
}
