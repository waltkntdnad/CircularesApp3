package com.circulares.difusion.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Representa una circular o instructivo almacenado en Firestore.
 *
 * Los nombres de las propiedades coinciden con los campos del documento.
 * El constructor sin argumentos (valores por defecto) es necesario para que
 * Firestore pueda deserializar automáticamente.
 */
data class Circular(
    @DocumentId
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    /** "imagen" o "pdf" */
    val tipo: String = "",
    /** URL de descarga en Firebase Storage */
    val url: String = "",
    val nombreArchivo: String = "",
    /** Fecha de publicación en milisegundos (epoch) */
    val fechaPublicacion: Long = 0L,
    /** Fecha de expiración = fechaPublicacion + 2 días */
    val fechaExpiracion: Long = 0L,
    /** Número total de visualizaciones registradas */
    val visualizaciones: Long = 0L,
    /** "activa" o "expirada" (estado automático por fecha) */
    val estado: String = "activa",
    /**
     * Activación manual del administrador. Si es false, la circular queda
     * desactivada y NO se muestra a los usuarios aunque siga dentro de los
     * 2 días. Por defecto true al publicar.
     */
    val activa: Boolean = true
) {
    /** True si la circular sigue dentro de su periodo de difusión (2 días). */
    fun estaVigente(ahora: Long = System.currentTimeMillis()): Boolean =
        ahora <= fechaExpiracion

    /** True si debe mostrarse a los usuarios: vigente Y activada por el admin. */
    fun esVisible(ahora: Long = System.currentTimeMillis()): Boolean =
        activa && estaVigente(ahora)
}
