package com.circulares.difusion.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.circulares.difusion.databinding.ActivityAdminBinding
import com.circulares.difusion.ui.reports.ReportsActivity
import com.circulares.difusion.ui.upload.UploadActivity
import com.google.android.material.snackbar.Snackbar

/**
 * Panel principal del administrador autenticado.
 */
class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val viewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.cardSubir.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }
        binding.cardReportes.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        binding.cardConfig.setOnClickListener { mostrarDialogoCredenciales() }
        binding.btnCerrarSesion.setOnClickListener {
            viewModel.cerrarSesion { finish() }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /** Pantalla de configuración: permite cambiar usuario y contraseña. */
    private fun mostrarDialogoCredenciales() {
        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }
        val inputUser = EditText(this).apply { hint = "Nuevo usuario" }
        val inputPass = EditText(this).apply {
            hint = "Nueva contraseña"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        contenedor.addView(inputUser)
        contenedor.addView(inputPass)

        AlertDialog.Builder(this)
            .setTitle("Configurar credenciales")
            .setView(contenedor)
            .setPositiveButton("Guardar") { _, _ ->
                val u = inputUser.text.toString()
                val p = inputPass.text.toString()
                if (u.isBlank() || p.isBlank()) {
                    Snackbar.make(binding.root, "Completa ambos campos.", Snackbar.LENGTH_LONG).show()
                } else {
                    viewModel.guardarNuevasCredenciales(u, p) {
                        Snackbar.make(binding.root, "Credenciales actualizadas.", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
