package com.circulares.difusion.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.circulares.difusion.data.model.Circular
import com.circulares.difusion.data.repository.CircularRepository
import com.circulares.difusion.data.repository.SessionManager
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla principal.
 * Observa las circulares vigentes en tiempo real desde Firestore y expone
 * el estado de sesión del administrador para mostrar/ocultar el acceso.
 */
class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = CircularRepository()
    private val sessionManager = SessionManager(app)

    /** Lista de circulares vigentes, actualizada automáticamente. */
    val circulares: LiveData<List<Circular>> =
        repository.observarCircularesVigentes().asLiveData()

    private val _esAdmin = MutableLiveData(false)
    val esAdmin: LiveData<Boolean> = _esAdmin

    fun refrescarSesion() {
        viewModelScope.launch {
            _esAdmin.value = sessionManager.estaLogueado()
        }
    }

    /** Registra una visualización solo si es la primera vez en este dispositivo. */
    fun registrarVisualizacion(circular: Circular) {
        viewModelScope.launch {
            val esNueva = sessionManager.marcarComoVista(circular.id)
            if (esNueva) {
                runCatching { repository.registrarVisualizacion(circular.id) }
            }
        }
    }
}
