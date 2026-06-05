package com.circulares.difusion.ui.reports

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.circulares.difusion.R
import com.circulares.difusion.databinding.ActivityReportsBinding
import com.circulares.difusion.ui.viewer.ViewerActivity
import com.circulares.difusion.util.Resultado
import com.google.android.material.snackbar.Snackbar

/**
 * Sección exclusiva del administrador: estadísticas y gestión de circulares
 * (previsualizar, activar/desactivar y eliminar).
 */
class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private val viewModel: ReportsViewModel by viewModels()

    private val adapter by lazy {
        ReportAdapter(
            onPrevisualizar = { circular -> ViewerActivity.iniciar(this, circular) },
            onToggleActiva = { circular -> viewModel.cambiarActiva(circular, !circular.activa) },
            onEliminar = { circular -> confirmarEliminacion(circular) }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.recyclerReportes.adapter = adapter

        viewModel.resumen.observe(this) { r ->
            binding.txtTotal.text = r.totalCirculares.toString()
            binding.txtVistas.text = r.totalVisualizaciones.toString()
            binding.txtActivas.text = r.activas.toString()
            binding.txtExpiradas.text = r.expiradas.toString()
        }

        viewModel.estado.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> binding.progress.visibility = View.VISIBLE
                is Resultado.Exito -> {
                    binding.progress.visibility = View.GONE
                    adapter.submitList(estado.datos)
                    binding.txtVacio.visibility =
                        if (estado.datos.isEmpty()) View.VISIBLE else View.GONE
                }
                is Resultado.Error -> {
                    binding.progress.visibility = View.GONE
                    Snackbar.make(binding.root, estado.mensaje, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.mensaje.observe(this) { msg ->
            if (msg != null) {
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                viewModel.mensajeMostrado()
            }
        }

        viewModel.cargar()
    }

    private fun confirmarEliminacion(circular: com.circulares.difusion.data.model.Circular) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirmar_eliminar_titulo)
            .setMessage(R.string.confirmar_eliminar_mensaje)
            .setPositiveButton(R.string.eliminar) { _, _ -> viewModel.eliminar(circular) }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
