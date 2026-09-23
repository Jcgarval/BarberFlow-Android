package com.example.barberflow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

// Recibimos una MutableList y una función (callback) para ejecutar al pulsar la papelera
class CitasAdapter(
    private val listaCitas: MutableList<Cita>,
    private val onEliminarClick: (Int, Int) -> Unit
) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textoServicio: TextView = itemView.findViewById(R.id.tv_item_servicio)
        val textoBarbero: TextView = itemView.findViewById(R.id.tv_item_barbero)
        val textoFecha: TextView = itemView.findViewById(R.id.tv_item_fecha)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btn_eliminar_cita)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(vista)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val citaActual = listaCitas[position]

        holder.textoServicio.text = "Servicio: " + citaActual.servicio.nombre
        holder.textoBarbero.text = "Barbero: " + citaActual.barbero.nombre

        try {
            val fechaSinMilisegundos = citaActual.fecha_hora.substringBefore(".")
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fechaParseada = formatoEntrada.parse(fechaSinMilisegundos)
            holder.textoFecha.text = fechaParseada?.let { formatoSalida.format(it) } ?: citaActual.fecha_hora
        } catch (e: Exception) {
            holder.textoFecha.text = citaActual.fecha_hora
        }

        // Detectamos el clic en la papelera y enviamos el ID de la cita y su posición
        holder.btnEliminar.setOnClickListener {
            onEliminarClick(citaActual.id, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = listaCitas.size

    // Función para borrar visualmente la cita sin tener que recargar toda la pantalla
    fun eliminarItem(posicion: Int) {
        listaCitas.removeAt(posicion)
        notifyItemRemoved(posicion)
    }
}