package com.circulares.difusion.ui.reports

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.circulares.difusion.R
import com.circulares.difusion.data.model.Circular
import com.circulares.difusion.databinding.ItemReporteBinding
import com.circulares.difusion.util.Config
import com.circulares.difusion.util.FechaUtil

/**
 * Adaptador de la lista de reportes con acciones de administrador:
 * previsualizar, activar/desactivar y eliminar.
 */
class ReportAdapter(
    private val onPrevisualizar: (Circular) -> Unit,
    private val onToggleActiva: (Circular) -> Unit,
    private val onEliminar: (Circular) -> Unit
) : ListAdapter<Circular, ReportAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemReporteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(c: Circular) {
            binding.txtTitulo.text = c.titulo
            binding.txtFechaEnvio.text = "Enviada: ${FechaUtil.formatear(c.fechaPublicacion)}"
            binding.txtExpira.text = "Vence: ${FechaUtil.formatear(c.fechaExpiracion)}"
            binding.txtVisualizaciones.text = "Visualizaciones: ${c.visualizaciones}"
            binding.txtTipo.text = if (c.tipo == Config.TIPO_PDF) "PDF" else "Imagen"

            // Estado: desactivada / expirada / activa
            val vigente = c.estaVigente()
            when {
                !c.activa -> {
                    binding.txtEstado.text = "DESACTIVADA"
                    binding.txtEstado.setBackgroundResource(R.drawable.bg_estado_desactivada)
                }
                vigente -> {
                    binding.txtEstado.text = "ACTIVA"
                    binding.txtEstado.setBackgroundResource(R.drawable.bg_estado_activa)
                }
                else -> {
                    binding.txtEstado.text = "EXPIRADA"
                    binding.txtEstado.setBackgroundResource(R.drawable.bg_estado_expirada)
                }
            }

            // Miniatura de vista previa
            if (c.tipo == Config.TIPO_PDF) {
                binding.imgPreview.setImageResource(R.drawable.ic_pdf)
                binding.imgPreview.scaleType = ImageView.ScaleType.CENTER_INSIDE
            } else {
                binding.imgPreview.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(binding.imgPreview)
                    .load(c.url)
                    .placeholder(R.drawable.ic_image)
                    .into(binding.imgPreview)
            }

            // Botón activar/desactivar (texto según estado actual)
            binding.btnToggle.text = if (c.activa) {
                binding.root.context.getString(R.string.desactivar)
            } else {
                binding.root.context.getString(R.string.activar)
            }

            // Listeners
            binding.contenidoPrincipal.setOnClickListener { onPrevisualizar(c) }
            binding.btnPrevisualizar.setOnClickListener { onPrevisualizar(c) }
            binding.btnToggle.setOnClickListener { onToggleActiva(c) }
            binding.btnEliminar.setOnClickListener { onEliminar(c) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemReporteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Circular>() {
            override fun areItemsTheSame(a: Circular, b: Circular) = a.id == b.id
            override fun areContentsTheSame(a: Circular, b: Circular) = a == b
        }
    }
}
