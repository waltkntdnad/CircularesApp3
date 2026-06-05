package com.circulares.difusion.ui.upload

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.circulares.difusion.data.repository.CircularRepository
import com.circulares.difusion.util.Config
import com.circulares.difusion.util.Resultado
import kotlinx.coroutines.launch

class UploadViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = CircularRepository()

    private val _estado = MutableLiveData<Resultado<String>>()
    val estado: LiveData<Resultado<String>> = _estado

    fun publicar(
        titulo: String,
        descripcion: String,
        uri: Uri?,
        mimeType: String?,
        nombreArchivo: String
    ) {
        if (titulo.isBlank()) {
            _estado.value = Resultado.Error("El título es obligatorio.")
            return
        }
        if (uri == null) {
            _estado.value = Resultado.Error("Selecciona un archivo (imagen o PDF).")
            return
        }
        val tipo = when {
            mimeType == "application/pdf" -> Config.TIPO_PDF
            mimeType?.startsWith("image/") == true -> Config.TIPO_IMAGEN
            else -> {
                _estado.value = Resultado.Error("Formato no soportado. Usa JPG, PNG o PDF.")
                return
            }
        }

        _estado.value = Resultado.Cargando
        viewModelScope.launch {
            try {
                val id = repository.publicarCircular(
                    titulo.trim(), descripcion.trim(), tipo, uri, nombreArchivo
                )
                _estado.value = Resultado.Exito(id)
            } catch (e: Exception) {
                _estado.value = Resultado.Error("Error al publicar: ${e.localizedMessage}")
            }
        }
    }
}
