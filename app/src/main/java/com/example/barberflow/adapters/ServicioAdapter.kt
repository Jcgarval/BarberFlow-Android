package com.example.barberflow.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barberflow.R
import com.example.barberflow.models.Servicio
import com.example.barberflow.models.formatearPrecio

class ServicioAdapter(
    private var servicios: List<Servicio>,
    private val onEditClick: (Servicio) -> Unit,
    private val onDeleteClick: (Servicio) -> Unit
) : RecyclerView.Adapter<ServicioAdapter.ServicioViewHolder>() {

    class ServicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreServicio)
        val tvDetalles: TextView = itemView.findViewById(R.id.tvDetallesServicio)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecioServicio)
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
        holder.tvDetalles.text = holder.itemView.context.getString(R.string.item_servicio_min, servicio.duracion_minutos)
        holder.tvPrecio.text = formatearPrecio(servicio.precio)

        holder.btnEditar.setOnClickListener { onEditClick(servicio) }
        holder.btnEliminar.setOnClickListener { onDeleteClick(servicio) }
    }

    private var recycler: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recycler = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        recycler = null
    }

    override fun getItemCount(): Int = servicios.size

    fun actualizarLista(nuevaLista: List<Servicio>) {
        servicios = nuevaLista
        notifyDataSetChanged()
        recycler?.scheduleLayoutAnimation()
    }
}
