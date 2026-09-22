package com.example.barberflow

    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import androidx.recyclerview.widget.RecyclerView

    class CitasAdapter(private val listaCitas: List<Cita>) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

        class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textoServicio = itemView.findViewById<TextView>(R.id.tv_item_servicio)
            val textoBarbero = itemView.findViewById<TextView>(R.id.tv_item_barbero)
            val textoFecha = itemView.findViewById<TextView>(R.id.tv_item_fecha)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
            val vista = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cita, parent, false)
            return CitaViewHolder(vista)
        }

        override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
            val citaActual = listaCitas[position]

            holder.textoServicio.text = "Servicio ID: " + citaActual.servicio_id.toString()
            holder.textoBarbero.text = "Barbero ID: " + citaActual.barbero_id.toString()
            holder.textoFecha.text = citaActual.fecha_hora
        }

        override fun getItemCount(): Int {
            return listaCitas.size
        }
    }
