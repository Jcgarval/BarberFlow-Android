package com.example.barberflow.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barberflow.R
import com.example.barberflow.models.CitaDetalle

class AdminCitasAdapter(private val citas: List<CitaDetalle>) : RecyclerView.Adapter<AdminCitasAdapter.AdminCitaViewHolder>() {

    class AdminCitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tv_cita_fecha)
        val tvDetalles: TextView = view.findViewById(R.id.tv_cita_detalles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminCitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita_admin, parent, false)
        return AdminCitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminCitaViewHolder, position: Int) {
        val cita = citas[position]
        val fechaLimpia = cita.fecha_hora.replace("T", " ")

        holder.tvFecha.text = "Fecha: $fechaLimpia"
        // Imprimimos los nombres directamente
        holder.tvDetalles.text = "Cliente: ${cita.cliente_nombre} | Barbero: ${cita.barbero_nombre} | Servicio: ${cita.servicio_nombre}"
    }

    override fun getItemCount() = citas.size
}