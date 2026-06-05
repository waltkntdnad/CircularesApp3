package com.circulares.difusion.data.repository

import com.circulares.difusion.util.Config

/**
 * Repositorio de autenticación del administrador.
 *
 * No usa Firebase Auth porque el requisito indica que solo el administrador
 * inicia sesión con un usuario y contraseña definidos en el código (o en la
 * pantalla de configuración). La validación es local.
 */
class AuthRepository(private val sessionManager: SessionManager) {

    /**
     * Valida usuario y contraseña.
     * Si hay credenciales personalizadas guardadas, se usan esas;
     * en caso contrario se usan las definidas en Config.
     */
    suspend fun validarCredenciales(user: String, pass: String): Boolean {
        val personalizadas = sessionManager.obtenerCredenciales()
        val (userValido, passValido) = personalizadas
            ?: (Config.ADMIN_USER to Config.ADMIN_PASSWORD)
        val ok = user.trim() == userValido && pass == passValido
        if (ok) sessionManager.setLogueado(true)
        return ok
    }

    suspend fun cerrarSesion() = sessionManager.setLogueado(false)

    suspend fun estaLogueado(): Boolean = sessionManager.estaLogueado()
}
