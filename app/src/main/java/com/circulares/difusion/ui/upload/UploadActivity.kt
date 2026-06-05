package com.circulares.difusion.ui.upload

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.circulares.difusion.R
import com.circulares.difusion.databinding.ActivityUploadBinding
import com.circulares.difusion.util.Resultado
import com.google.android.material.snackbar.Snackbar

/**
 * Pantalla donde el administrador sube una circular (imagen JPG/PNG o PDF).
 * Incluye VISTA PREVIA del archivo seleccionado antes de publicar.
 */
class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private val viewModel: UploadViewModel by viewModels()

    private var uriSeleccionada: Uri? = null
    private var mimeType: String? = null
    private var nombreArchivo: String = "archivo"

    // Selector de documentos: acepta imágenes y PDF.
    private val selector = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            uriSeleccionada = uri
            mimeType = contentResolver.getType(uri)
            nombreArchivo = obtenerNombre(uri)
            binding.txtArchivo.text = nombreArchivo
            mostrarVistaPrevia(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSeleccionar.setOnClickListener {
            selector.launch(arrayOf("image/jpeg", "image/png", "application/pdf"))
        }

        // Abrir el archivo en el visor del dispositivo (vista previa completa)
        binding.btnPrevisualizar.setOnClickListener { abrirVistaPreviaCompleta() }

        binding.btnPublicar.setOnClickListener {
            viewModel.publicar(
                binding.editTitulo.text.toString(),
                binding.editDescripcion.text.toString(),
                uriSeleccionada,
                mimeType,
                nombreArchivo
            )
        }

        viewModel.estado.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.btnPublicar.isEnabled = false
                }
                is Resultado.Exito -> {
                    binding.progress.visibility = View.GONE
                    Snackbar.make(binding.root, "Circular publicada y difundida.", Snackbar.LENGTH_LONG).show()
                    finish()
                }
                is Resultado.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.btnPublicar.isEnabled = true
                    Snackbar.make(binding.root, estado.mensaje, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    /** Vista previa en línea: imagen real o icono de PDF. */
    private fun mostrarVistaPrevia(uri: Uri) {
        binding.cardPreview.visibility = View.VISIBLE
        binding.btnPrevisualizar.visibility = View.VISIBLE

        val esPdf = mimeType == "application/pdf"
        if (esPdf) {
            binding.imgPreview.scaleType = ImageView.ScaleType.CENTER_INSIDE
            binding.imgPreview.setImageResource(R.drawable.ic_pdf)
        } else {
            binding.imgPreview.scaleType = ImageView.ScaleType.FIT_CENTER
            Glide.with(this).load(uri).into(binding.imgPreview)
        }
    }

    /** Abre el archivo seleccionado con el visor nativo del dispositivo. */
    private fun abrirVistaPreviaCompleta() {
        val uri = uriSeleccionada ?: return
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.vista_previa)))
        } catch (e: Exception) {
            Snackbar.make(
                binding.root,
                "No hay una app para abrir este tipo de archivo.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun obtenerNombre(uri: Uri): String {
        var nombre = "archivo"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) {
                nombre = cursor.getString(idx)
            }
        }
        return nombre
    }
}
