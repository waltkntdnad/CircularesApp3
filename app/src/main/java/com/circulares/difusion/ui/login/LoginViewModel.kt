package com.circulares.difusion.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.circulares.difusion.data.repository.AuthRepository
import com.circulares.difusion.data.repository.SessionManager
import com.circulares.difusion.util.Resultado
import kotlinx.coroutines.launch

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val authRepository = AuthRepository(SessionManager(app))

    private val _estado = MutableLiveData<Resultado<Boolean>>()
    val estado: androidx.lifecycle.LiveData<Resultado<Boolean>> = _estado

    fun iniciarSesion(user: String, pass: String) {
        if (user.isBlank() || pass.isBlank()) {
            _estado.value = Resultado.Error("Ingresa usuario y contraseña.")
            return
        }
        _estado.value = Resultado.Cargando
        viewModelScope.launch {
            val ok = authRepository.validarCredenciales(user, pass)
            _estado.value = if (ok) {
                Resultado.Exito(true)
            } else {
                Resultado.Error("Usuario o contraseña incorrectos.")
            }
        }
    }
}
