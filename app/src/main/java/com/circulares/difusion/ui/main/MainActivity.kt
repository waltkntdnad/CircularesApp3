package com.circulares.difusion.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.circulares.difusion.databinding.ActivityMainBinding
import com.circulares.difusion.ui.admin.AdminActivity
import com.circulares.difusion.ui.login.LoginActivity
import com.circulares.difusion.ui.viewer.ViewerActivity

/**
 * Pantalla principal que ven TODOS los usuarios (sin login).
 * Muestra automáticamente las circulares vigentes en tiempo real.
 * El acceso de administrador se ofrece mediante un botón en la barra.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: CircularAdapter

    private val permisoNotif = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* resultado ignorado: la app funciona con o sin permiso */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        solicitarPermisoNotificaciones()
        configurarLista()
        observar()

        // Botón flotante: acceso del administrador
        binding.fabAdmin.setOnClickListener { abrirAdmin() }
        binding.swipeRefresh.setOnRefreshListener {
            // El listener de Firestore ya es en tiempo real; solo cerramos el spinner.
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refrescarSesion()
    }

    private fun configurarLista() {
        adapter = CircularAdapter { circular ->
            viewModel.registrarVisualizacion(circular)
            ViewerActivity.iniciar(this, circular)
        }
        binding.recyclerCirculares.adapter = adapter
    }

    private fun observar() {
        viewModel.circulares.observe(this) { lista ->
            adapter.submitList(lista)
            binding.txtVacio.visibility =
                if (lista.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE

            // Visualización automática: abre la circular más reciente al entrar.
            if (lista.isNotEmpty() && !yaMostroAutomatico) {
                yaMostroAutomatico = true
                val ultima = lista.first()
                viewModel.registrarVisualizacion(ultima)
                ViewerActivity.iniciar(this, ultima)
            }
        }
    }

    private var yaMostroAutomatico = false

    private fun abrirAdmin() {
        // Si ya hay sesión activa, va directo al panel; si no, al login.
        val destino = if (viewModel.esAdmin.value == true) {
            AdminActivity::class.java
        } else {
            LoginActivity::class.java
        }
        startActivity(Intent(this, destino))
    }

    private fun solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedido = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!concedido) permisoNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
