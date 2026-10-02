package com.example.barberflow.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barberflow.R
import com.example.barberflow.models.Cita
import com.example.barberflow.models.EstadoCita
import com.example.barberflow.models.aplicarBadgeEstado
import com.example.barberflow.models.formatearFechaHora

class CitasAdapter(
    private var listaCitas: List<Cita>,
    private val onCitaClick: (Cita) -> Unit
) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFecha: TextView = itemView.findViewById(R.id.tv_cita_fecha)
        val tvDetalles: TextView = itemView.findViewById(R.id.tv_cita_detalles)
        val tvEstado: TextView = itemView.findViewById(R.id.tv_cita_estado)
        val tvAccion: TextView = itemView.findViewById(R.id.tv_cita_accion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita_admin, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = listaCitas[position]

        holder.tvFecha.text = formatearFechaHora(cita.fecha_hora)
        holder.tvDetalles.text = "${cita.servicio.nombre} · con ${cita.barbero.nombre}"
        aplicarBadgeEstado(holder.tvEstado, cita.estado)

        val seCancela = EstadoCita.sePuedeCancelar(cita.estado)
        holder.tvAccion.visibility = if (seCancela) View.VISIBLE else View.GONE
        holder.itemView.alpha = if (cita.estado == EstadoCita.CANCELADA) 0.6f else 1f

        holder.itemView.setOnClickListener { onCitaClick(cita) }
    }

    override fun getItemCount(): Int = listaCitas.size

    fun actualizarLista(nuevaLista: List<Cita>) {
        listaCitas = nuevaLista
        notifyDataSetChanged()
    }
}
