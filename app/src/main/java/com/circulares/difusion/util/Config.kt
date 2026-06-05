package com.circulares.difusion.util

/**
 * Configuración central de la aplicación.
 *
 * CREDENCIALES DEL ADMINISTRADOR
 * ------------------------------------------------------------------
 * Las credenciales por defecto están definidas aquí en el código.
 * Para cambiarlas tienes DOS opciones:
 *   1) Modificar directamente ADMIN_USER y ADMIN_PASSWORD aquí abajo
 *      y recompilar.
 *   2) Usar la pantalla de configuración dentro del panel de
 *      administrador (botón "Configuración"), que guarda nuevas
 *      credenciales de forma local con DataStore. Si existen
 *      credenciales guardadas, estas tienen prioridad sobre las del
 *      código (ver AuthRepository).
 * ------------------------------------------------------------------
 */
object Config {

    // --- Credenciales por defecto del administrador ---
    const val ADMIN_USER = "admin"
    const val ADMIN_PASSWORD = "admin123"

    // --- Parámetros de difusión ---
    /** Intervalo de repetición de la visualización: 4 horas. */
    const val INTERVALO_HORAS: Long = 4

    /** Duración total de la difusión: 2 días desde la publicación. */
    const val DURACION_DIAS: Long = 2

    /** Milisegundos equivalentes a la duración de la difusión (2 días). */
    const val DURACION_MILLIS: Long = DURACION_DIAS * 24L * 60L * 60L * 1000L

    // --- Colecciones de Firestore ---
    const val COLECCION_CIRCULARES = "circulares"

    // --- Carpeta en Firebase Storage ---
    const val CARPETA_STORAGE = "circulares"

    // --- Tipos de archivo ---
    const val TIPO_IMAGEN = "imagen"
    const val TIPO_PDF = "pdf"

    // --- Estados de difusión ---
    const val ESTADO_ACTIVA = "activa"
    const val ESTADO_EXPIRADA = "expirada"
}
