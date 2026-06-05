package com.circulares.difusion.ui.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.circulares.difusion.data.model.Circular
import com.circulares.difusion.data.repository.CircularRepository
import com.circulares.difusion.util.Resultado
import kotlinx.coroutines.launch

/** Resumen agregado para las estadísticas del administrador. */
data class ResumenReporte(
    val totalCirculares: Int,
    val totalVisualizaciones: Long,
    val activas: Int,
    val expiradas: Int,
    val desactivadas: Int
)

class ReportsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = CircularRepository()

    private val _estado = MutableLiveData<Resultado<List<Circular>>>()
    val estado: LiveData<Resultado<List<Circular>>> = _estado

    private val _resumen = MutableLiveData<ResumenReporte>()
    val resumen: LiveData<ResumenReporte> = _resumen

    /** Mensajes puntuales para confirmar acciones (eliminar/activar). */
    private val _mensaje = MutableLiveData<String?>()
    val mensaje: LiveData<String?> = _mensaje

    fun cargar() {
        _estado.value = Resultado.Cargando
        viewModelScope.launch {
            try {
                repository.actualizarEstados()
                val lista = repository.obtenerTodas()
                _resumen.value = ResumenReporte(
                    totalCirculares = lista.size,
                    totalVisualizaciones = lista.sumOf { it.visualizaciones },
                    // "Activas" = visibles ahora (activadas y vigentes)
                    activas = lista.count { it.esVisible() },
                    expiradas = lista.count { !it.estaVigente() },
                    desactivadas = lista.count { !it.activa && it.estaVigente() }
                )
                _estado.value = Resultado.Exito(lista)
            } catch (e: Exception) {
                _estado.value = Resultado.Error("Error al cargar reportes: ${e.localizedMessage}")
            }
        }
    }

    /** Activa o desactiva una circular y recarga la lista. */
    fun cambiarActiva(circular: Circular, activa: Boolean) {
        viewModelScope.launch {
            try {
                repository.establecerActiva(circular.id, activa)
                _mensaje.value = if (activa) "Circular activada." else "Circular desactivada."
                cargar()
            } catch (e: Exception) {
                _mensaje.value = "No se pudo actualizar: ${e.localizedMessage}"
            }
        }
    }

    /** Elimina definitivamente una circular (documento + archivo) y recarga. */
    fun eliminar(circular: Circular) {
        viewModelScope.launch {
            try {
                repository.eliminarCircular(circular)
                _mensaje.value = "Circular eliminada."
                cargar()
            } catch (e: Exception) {
                _mensaje.value = "No se pudo eliminar: ${e.localizedMessage}"
            }
        }
    }

    fun mensajeMostrado() {
        _mensaje.value = null
    }
}
