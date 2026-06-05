package com.circulares.difusion.ui.viewer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.circulares.difusion.data.model.Circular
import com.circulares.difusion.databinding.ActivityViewerBinding
import com.circulares.difusion.util.Config
import com.circulares.difusion.util.FechaUtil
import java.net.URLEncoder

/**
 * Visor a pantalla completa de una circular CON ZOOM.
 *
 * Tanto imágenes como PDF se muestran dentro de un WebView con el zoom nativo
 * habilitado (pellizco/pinch y doble toque):
 *  - Imagen: se inyecta en una página HTML mínima con viewport que permite zoom.
 *  - PDF: se renderiza con el visor de Google Docs.
 *
 * Usar WebView para ambos evita añadir librerías externas y da una experiencia
 * de zoom uniforme.
 */
class ViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewerBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val titulo = intent.getStringExtra(EXTRA_TITULO).orEmpty()
        val descripcion = intent.getStringExtra(EXTRA_DESC).orEmpty()
        val tipo = intent.getStringExtra(EXTRA_TIPO).orEmpty()
        val url = intent.getStringExtra(EXTRA_URL).orEmpty()
        val fecha = intent.getLongExtra(EXTRA_FECHA, 0L)

        supportActionBar?.title = titulo
        binding.txtDescripcion.text = descripcion
        binding.txtFecha.text = "Publicada: ${FechaUtil.formatear(fecha)}"

        configurarWebViewConZoom()

        if (tipo == Config.TIPO_PDF) {
            mostrarPdf(url)
        } else {
            mostrarImagen(url)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configurarWebViewConZoom() {
        with(binding.webViewer.settings) {
            javaScriptEnabled = true
            // Zoom por pellizco y doble toque
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false   // sin los botones +/- en pantalla
            useWideViewPort = true
            loadWithOverviewMode = true
        }
    }

    /** Muestra la imagen dentro de una página HTML que permite hacer zoom. */
    private fun mostrarImagen(url: String) {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport"
                    content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=6.0, user-scalable=yes">
              <style>
                html,body{margin:0;padding:0;height:100%;background:#0d1b2a;}
                .wrap{display:flex;align-items:center;justify-content:center;min-height:100%;}
                img{max-width:100%;height:auto;display:block;}
              </style>
            </head>
            <body>
              <div class="wrap"><img src="$url"/></div>
            </body>
            </html>
        """.trimIndent()
        binding.webViewer.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
    }

    /** Muestra el PDF con el visor de Google Docs (también permite zoom). */
    private fun mostrarPdf(url: String) {
        val encoded = URLEncoder.encode(url, "UTF-8")
        val visor = "https://docs.google.com/gview?embedded=true&url=$encoded"
        binding.webViewer.loadUrl(visor)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        private const val EXTRA_TITULO = "extra_titulo"
        private const val EXTRA_DESC = "extra_desc"
        private const val EXTRA_TIPO = "extra_tipo"
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_FECHA = "extra_fecha"

        fun iniciar(context: Context, circular: Circular) {
            val intent = Intent(context, ViewerActivity::class.java).apply {
                putExtra(EXTRA_TITULO, circular.titulo)
                putExtra(EXTRA_DESC, circular.descripcion)
                putExtra(EXTRA_TIPO, circular.tipo)
                putExtra(EXTRA_URL, circular.url)
                putExtra(EXTRA_FECHA, circular.fechaPublicacion)
            }
            context.startActivity(intent)
        }
    }
}
