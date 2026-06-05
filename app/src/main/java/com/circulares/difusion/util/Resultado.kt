package com.circulares.difusion.util

/**
 * Envoltura genérica para representar el resultado de una operación
 * asíncrona (cargando, éxito o error). Usada por los ViewModels.
 */
sealed class Resultado<out T> {
    object Cargando : Resultado<Nothing>()
    data class Exito<T>(val datos: T) : Resultado<T>()
    data class Error(val mensaje: String) : Resultado<Nothing>()
}
