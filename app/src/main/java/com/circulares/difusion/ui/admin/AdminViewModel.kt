package com.circulares.difusion.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.circulares.difusion.data.repository.AuthRepository
import com.circulares.difusion.data.repository.SessionManager
import kotlinx.coroutines.launch

class AdminViewModel(app: Application) : AndroidViewModel(app) {

    private val sessionManager = SessionManager(app)
    private val authRepository = AuthRepository(sessionManager)

    fun cerrarSesion(onHecho: () -> Unit) {
        viewModelScope.launch {
            authRepository.cerrarSesion()
            onHecho()
        }
    }

    fun guardarNuevasCredenciales(user: String, pass: String, onHecho: () -> Unit) {
        viewModelScope.launch {
            sessionManager.guardarCredenciales(user.trim(), pass)
            onHecho()
        }
    }
}
