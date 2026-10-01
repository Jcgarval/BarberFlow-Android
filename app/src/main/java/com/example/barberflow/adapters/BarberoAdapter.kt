package com.example.barberflow.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barberflow.R
import com.example.barberflow.models.Barbero

class BarberoAdapter(
    private var barberos: List<Barbero>,
    // Pasamos funciones como parámetros para que la Activity decida qué hacer al hacer clic
    private val onEditClick: (Barbero) -> Unit,
    private val onDeleteClick: (Barbero) -> Unit
) : RecyclerView.Adapter<BarberoAdapter.BarberoViewHolder>() {

    // 1. El ViewHolder: Busca los elementos dentro de tu item_barbero.xml
    class BarberoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreBarbero)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditarBarbero)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminarBarbero)
    }

    // 2. onCreateViewHolder: "Infla" (convierte el XML en código) el molde por cada fila nueva
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarberoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_barbero, parent, false)
        return BarberoViewHolder(view)
    }

    // 3. onBindViewHolder: Conecta los datos del barbero con los elementos visuales de la fila
    override fun onBindViewHolder(holder: BarberoViewHolder, position: Int) {
        val barbero = barberos[position]

        // Ponemos el nombre en el TextView
        holder.tvNombre.text = barbero.nombre

        // Configuramos los clics de los botones
        holder.btnEditar.setOnClickListener { onEditClick(barbero) }
        holder.btnEliminar.setOnClickListener { onDeleteClick(barbero) }
    }

    // 4. getItemCount: Le dice al RecyclerView cuántas filas tiene que dibujar en total
    override fun getItemCount(): Int {
        return barberos.size
    }

    // 5. Función extra para actualizar la lista cuando descarguemos datos de la API
    fun actualizarLista(nuevaLista: List<Barbero>) {
        barberos = nuevaLista
        notifyDataSetChanged() // Avisa al RecyclerView de que hay datos nuevos y debe repintarse
    }
}