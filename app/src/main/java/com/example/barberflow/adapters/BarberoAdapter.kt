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

    class BarberoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvInicial: TextView = itemView.findViewById(R.id.tvInicialBarbero)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreBarbero)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditarBarbero)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminarBarbero)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarberoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_barbero, parent, false)
        return BarberoViewHolder(view)
    }

    override fun onBindViewHolder(holder: BarberoViewHolder, position: Int) {
        val barbero = barberos[position]

        holder.tvNombre.text = barbero.nombre
        // Avatar con la inicial del nombre
        holder.tvInicial.text = barbero.nombre.trim().take(1).uppercase()

        holder.btnEditar.setOnClickListener { onEditClick(barbero) }
        holder.btnEliminar.setOnClickListener { onDeleteClick(barbero) }
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

    override fun getItemCount(): Int = barberos.size

    fun actualizarLista(nuevaLista: List<Barbero>) {
        barberos = nuevaLista
        notifyDataSetChanged()
        recycler?.scheduleLayoutAnimation()
    }
}
