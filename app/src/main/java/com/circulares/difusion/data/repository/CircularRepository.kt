package com.circulares.difusion.data.repository

import android.net.Uri
import com.circulares.difusion.data.model.Circular
import com.circulares.difusion.util.Config
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Repositorio central de circulares.
 * Encapsula todo el acceso a Firebase Firestore (datos) y Firebase Storage
 * (archivos). Es la única fuente de verdad para los ViewModels.
 */
class CircularRepository {

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val coleccion = db.collection(Config.COLECCION_CIRCULARES)

    /**
     * Sube un archivo (imagen o PDF) a Storage y crea el documento en Firestore.
     * @return el id del documento creado.
     */
    suspend fun publicarCircular(
        titulo: String,
        descripcion: String,
        tipo: String,
        uri: Uri,
        nombreArchivo: String
    ): String {
        // 1. Subir archivo a Storage
        val extension = if (tipo == Config.TIPO_PDF) "pdf" else "img"
        val ruta = "${Config.CARPETA_STORAGE}/${UUID.randomUUID()}_$nombreArchivo"
        val ref = storage.reference.child(ruta)
        ref.putFile(uri).await()
        val url = ref.downloadUrl.await().toString()

        // 2. Crear documento en Firestore
        val ahora = System.currentTimeMillis()
        val circular = Circular(
            titulo = titulo,
            descripcion = descripcion,
            tipo = tipo,
            url = url,
            nombreArchivo = nombreArchivo,
            fechaPublicacion = ahora,
            fechaExpiracion = ahora + Config.DURACION_MILLIS,
            visualizaciones = 0L,
            estado = Config.ESTADO_ACTIVA
        )
        val doc = coleccion.add(circular).await()
        return doc.id
    }

    /**
     * Flujo en tiempo real con las circulares VIGENTES (no expiradas),
     * ordenadas de la más reciente a la más antigua.
     * Se actualiza automáticamente en todos los dispositivos cuando
     * el administrador publica una nueva circular.
     */
    fun observarCircularesVigentes(): Flow<List<Circular>> = callbackFlow {
        val ahora = System.currentTimeMillis()
        val registro = coleccion
            .whereGreaterThanOrEqualTo("fechaExpiracion", ahora)
            .orderBy("fechaExpiracion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lista = snapshot?.toObjects<Circular>() ?: emptyList()
                // Solo circulares activadas por el admin, ordenadas por fecha desc.
                // (El filtro 'activa' se hace en cliente para no requerir índice compuesto.)
                trySend(
                    lista.filter { it.activa }
                        .sortedByDescending { it.fechaPublicacion }
                )
            }
        awaitClose { registro.remove() }
    }

    /** Lista (una sola vez) las circulares vigentes. Usado por el WorkManager. */
    suspend fun obtenerCircularesVigentes(): List<Circular> {
        val ahora = System.currentTimeMillis()
        val snapshot = coleccion
            .whereGreaterThanOrEqualTo("fechaExpiracion", ahora)
            .get()
            .await()
        return snapshot.toObjects<Circular>()
            .filter { it.activa }
            .sortedByDescending { it.fechaPublicacion }
    }

    /** Lista TODAS las circulares (para la sección de reportes del admin). */
    suspend fun obtenerTodas(): List<Circular> {
        val snapshot = coleccion
            .orderBy("fechaPublicacion", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.toObjects<Circular>()
    }

    /** Incrementa de forma atómica el contador de visualizaciones. */
    suspend fun registrarVisualizacion(circularId: String) {
        coleccion.document(circularId)
            .update("visualizaciones", FieldValue.increment(1))
            .await()
    }

    /**
     * Recalcula y actualiza el estado (activa/expirada) de las circulares.
     * Se ejecuta periódicamente desde el WorkManager.
     */
    suspend fun actualizarEstados() {
        val ahora = System.currentTimeMillis()
        val snapshot = coleccion
            .whereEqualTo("estado", Config.ESTADO_ACTIVA)
            .get()
            .await()
        for (doc in snapshot.documents) {
            val expira = doc.getLong("fechaExpiracion") ?: 0L
            if (ahora > expira) {
                doc.reference.update("estado", Config.ESTADO_EXPIRADA).await()
            }
        }
    }

    /** Activa o desactiva manualmente una circular (afecta su visibilidad). */
    suspend fun establecerActiva(circularId: String, activa: Boolean) {
        coleccion.document(circularId)
            .update("activa", activa)
            .await()
    }

    /** Elimina una circular de Firestore (y su archivo en Storage). */
    suspend fun eliminarCircular(circular: Circular) {
        coleccion.document(circular.id).delete().await()
        runCatching {
            storage.getReferenceFromUrl(circular.url).delete().await()
        }
    }
}
