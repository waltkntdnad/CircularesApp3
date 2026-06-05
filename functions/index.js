/**
 * Cloud Function (2ª generación) para la app Circulares.
 *
 * Se dispara automáticamente cada vez que se crea un documento nuevo en la
 * colección `circulares` de Firestore y envía una notificación push (FCM)
 * al topic "circulares". Todos los dispositivos suscritos la reciben al
 * instante, incluso con la app cerrada.
 */
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");
const { initializeApp } = require("firebase-admin/app");
const { getMessaging } = require("firebase-admin/messaging");

// Inicializa el SDK de administrador
initializeApp();

// Región cercana (ajusta si tu Firestore está en otra región).
setGlobalOptions({ region: "southamerica-east1", maxInstances: 5 });

exports.notificarCircular = onDocumentCreated("circulares/{id}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) {
    console.log("Evento sin datos; se omite.");
    return;
  }

  const circular = snapshot.data();
  const titulo = circular.titulo || "Nueva circular";
  const descripcion = circular.descripcion || "Toca para visualizar.";

  const mensaje = {
    topic: "circulares",
    // Bloque notification: el sistema la muestra aunque la app esté cerrada.
    notification: {
      title: `Nueva circular: ${titulo}`,
      body: descripcion,
    },
    // Datos extra (por si el cliente quiere abrir la circular directamente).
    data: {
      circularId: event.params.id,
      titulo: titulo,
      descripcion: descripcion,
      tipo: circular.tipo || "",
    },
    android: {
      priority: "high",
      notification: {
        channelId: "canal_circulares",
      },
    },
  };

  try {
    const respuesta = await getMessaging().send(mensaje);
    console.log("Notificación enviada:", respuesta);
  } catch (error) {
    console.error("Error al enviar la notificación:", error);
  }
});
