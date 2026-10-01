package com.example.barberflow.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barberflow.R
import com.example.barberflow.models.Servicio

class ServicioAdapter(
    private var servicios: List<Servicio>,
    private val onEditClick: (Servicio) -> Unit,
    private val onDeleteClick: (Servicio) -> Unit
) : RecyclerView.Adapter<ServicioAdapter.ServicioViewHolder>() {

    class ServicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreServicio)
        val tvDetalles: TextView = itemView.findViewById(R.id.tvDetallesServicio)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditarServicio)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminarServicio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_servicio, parent, false)
        return ServicioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServicioViewHolder, position: Int) {
        val servicio = servicios[position]

        holder.tvNombre.text = servicio.nombre
        // CORRECCIÓN: Usamos duracion_minutos
        holder.tvDetalles.text = "${servicio.duracion_minutos} min - ${servicio.precio}€"

        holder.btnEditar.setOnClickListener { onEditClick(servicio) }
        holder.btnEliminar.setOnClickListener { onDeleteClick(servicio) }
    }

    override fun getItemCount(): Int = servicios.size

    fun actualizarLista(nuevaLista: List<Servicio>) {
        servicios = nuevaLista
        notifyDataSetChanged()
    }
}