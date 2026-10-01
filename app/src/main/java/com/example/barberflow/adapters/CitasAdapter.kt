package com.example.barberflow.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barberflow.R
import com.example.barberflow.models.Cita

class CitasAdapter(
    private var listaCitas: MutableList<Cita>,
    private val onDeleteClick: (Int, Int) -> Unit // Recibe (idCita, posicionEnLista)
) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFecha: TextView = itemView.findViewById(R.id.tv_cita_fecha)
        val tvDetalles: TextView = itemView.findViewById(R.id.tv_cita_detalles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        // Usamos tu layout real item_cita_admin
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita_admin, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = listaCitas[position]

        // Rellenamos con los campos de tu modelo Cita
        holder.tvFecha.text = "Fecha: ${cita.fecha_hora}"
        holder.tvDetalles.text = "Detalles de la cita ID: ${cita.id}" // O los campos que muestres habitualmente

        // Al pulsar en la tarjeta de la cita se dispara el evento de borrado/gestión
        holder.itemView.setOnClickListener {
            onDeleteClick(cita.id, position)
        }
    }

    override fun getItemCount(): Int = listaCitas.size

    // Método que llama HistorialActivity para borrar la tarjeta visualmente al instante
    fun eliminarItem(position: Int) {
        if (position in 0 until listaCitas.size) {
            listaCitas.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, listaCitas.size)
        }
    }

    fun actualizarLista(nuevaLista: List<Cita>) {
        listaCitas = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }
}