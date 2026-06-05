package com.circulares.difusion.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.circulares.difusion.databinding.ActivityLoginBinding
import com.circulares.difusion.ui.admin.AdminActivity
import com.circulares.difusion.util.Resultado

/**
 * Pantalla de inicio de sesión exclusiva del administrador.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnIngresar.setOnClickListener {
            viewModel.iniciarSesion(
                binding.editUsuario.text.toString(),
                binding.editPassword.text.toString()
            )
        }

        viewModel.estado.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.btnIngresar.isEnabled = false
                }
                is Resultado.Exito -> {
                    binding.progress.visibility = View.GONE
                    startActivity(Intent(this, AdminActivity::class.java))
                    finish()
                }
                is Resultado.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.btnIngresar.isEnabled = true
                    binding.txtError.visibility = View.VISIBLE
                    binding.txtError.text = estado.mensaje
                }
            }
        }
    }
}
