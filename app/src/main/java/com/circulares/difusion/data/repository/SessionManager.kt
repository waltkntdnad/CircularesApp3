package com.circulares.difusion.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extensión que crea un único DataStore para toda la app.
private val Context.dataStore by preferencesDataStore(name = "circulares_prefs")

/**
 * Gestiona el estado local del dispositivo mediante DataStore:
 *  - Sesión del administrador.
 *  - Credenciales personalizadas (pantalla de configuración).
 *  - Registro de qué circulares ya fueron vistas en ESTE dispositivo
 *    (para no contar visualizaciones duplicadas).
 */
class SessionManager(private val context: Context) {

    companion object {
        private val KEY_LOGUEADO = booleanPreferencesKey("admin_logueado")
        private val KEY_USER = stringPreferencesKey("admin_user")
        private val KEY_PASS = stringPreferencesKey("admin_pass")
        private val KEY_VISTAS = stringSetPreferencesKey("circulares_vistas")
    }

    suspend fun setLogueado(valor: Boolean) {
        context.dataStore.edit { it[KEY_LOGUEADO] = valor }
    }

    suspend fun estaLogueado(): Boolean =
        context.dataStore.data.map { it[KEY_LOGUEADO] ?: false }.first()

    /** Devuelve credenciales personalizadas o null si no se han configurado. */
    suspend fun obtenerCredenciales(): Pair<String, String>? {
        val prefs = context.dataStore.data.first()
        val user = prefs[KEY_USER]
        val pass = prefs[KEY_PASS]
        return if (user != null && pass != null) user to pass else null
    }

    suspend fun guardarCredenciales(user: String, pass: String) {
        context.dataStore.edit {
            it[KEY_USER] = user
            it[KEY_PASS] = pass
        }
    }

    /** Marca una circular como vista en este dispositivo. Devuelve true si era nueva. */
    suspend fun marcarComoVista(circularId: String): Boolean {
        val vistas = context.dataStore.data.map { it[KEY_VISTAS] ?: emptySet() }.first()
        if (vistas.contains(circularId)) return false
        context.dataStore.edit {
            it[KEY_VISTAS] = vistas + circularId
        }
        return true
    }
}
