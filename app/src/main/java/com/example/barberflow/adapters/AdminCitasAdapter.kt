package com.example.barberflow.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.barberflow.R
import com.example.barberflow.models.CitaDetalle
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AdminCitasAdapter(
    private var listaCitas: List<CitaDetalle>,
    private val onDeleteClick: (CitaDetalle) -> Unit
) : RecyclerView.Adapter<AdminCitasAdapter.CitaViewHolder>() {

    class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvServicio: TextView = itemView.findViewById(R.id.tv_item_servicio)
        val tvBarbero: TextView = itemView.findViewById(R.id.tv_item_barbero)
        val tvFecha: TextView = itemView.findViewById(R.id.tv_item_fecha)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btn_eliminar_cita)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = listaCitas[position]

        holder.tvServicio.text = cita.servicio_nombre
        holder.tvBarbero.text = "Barbero: ${cita.barbero_nombre} | Cliente: ${cita.cliente_nombre}"

        // Aplicamos el formato dd/MM/yyyy HH:mm usando la función auxiliar
        val fechaLegible = formatearFecha(cita.fecha_hora)
        holder.tvFecha.text = "📅 $fechaLegible"

        holder.btnEliminar.setOnClickListener {
            onDeleteClick(cita)
        }
    }

    override fun getItemCount(): Int {
        return listaCitas.size
    }

    fun actualizarLista(nuevaLista: List<CitaDetalle>) {
        listaCitas = nuevaLista
        notifyDataSetChanged()
    }

    // Función auxiliar para traducir la fecha ISO al formato visual deseado
    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatearFecha(fechaIso: String): String {
        return try {
            val fechaParseada = LocalDateTime.parse(fechaIso)
            val formateador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            fechaParseada.format(formateador)
        } catch (e: Exception) {
            // Si la fecha viene mal formada desde la API, mostramos la original por seguridad
            fechaIso
        }
    }
}