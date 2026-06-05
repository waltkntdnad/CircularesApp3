package com.circulares.difusion.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.circulares.difusion.R
import com.circulares.difusion.data.model.Circular
import com.circulares.difusion.databinding.ItemCircularBinding
import com.circulares.difusion.util.Config
import com.circulares.difusion.util.FechaUtil

/**
 * Adaptador para la lista de circulares vigentes.
 * Muestra una vista previa (imagen) o un icono de PDF según el tipo.
 */
class CircularAdapter(
    private val onClick: (Circular) -> Unit
) : ListAdapter<Circular, CircularAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemCircularBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(circular: Circular) {
            binding.txtTitulo.text = circular.titulo
            binding.txtDescripcion.text = circular.descripcion
            binding.txtFecha.text = "Publicada: ${FechaUtil.formatear(circular.fechaPublicacion)}"

            if (circular.tipo == Config.TIPO_PDF) {
                binding.imgPreview.setImageResource(R.drawable.ic_pdf)
                binding.imgPreview.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            } else {
                Glide.with(binding.imgPreview)
                    .load(circular.url)
                    .placeholder(R.drawable.ic_image)
                    .centerCrop()
                    .into(binding.imgPreview)
            }
            binding.root.setOnClickListener { onClick(circular) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCircularBinding.inflate(
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
