package com.circulares.difusion.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Funciones de formato de fechas para mostrar al usuario. */
object FechaUtil {

    private val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))
    private val formatoCorto = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

    fun formatear(millis: Long): String =
        if (millis <= 0) "-" else formato.format(Date(millis))

    fun formatearCorto(millis: Long): String =
        if (millis <= 0) "-" else formatoCorto.format(Date(millis))
}
