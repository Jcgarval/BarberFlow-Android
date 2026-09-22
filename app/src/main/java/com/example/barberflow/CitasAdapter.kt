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

            try {
                val fechaSinMilisegundos = citaActual.fecha_hora.substringBefore(".")
                val formatoEntrada = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                val formatoSalida = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                val fechaParseada = formatoEntrada.parse(fechaSinMilisegundos)
                if (fechaParseada != null) {
                    holder.textoFecha.text = formatoSalida.format(fechaParseada)
                } else {
                    holder.textoFecha.text = citaActual.fecha_hora
            }
        } catch (e: Exception) {
                holder.textoFecha.text = citaActual.fecha_hora
        }
        }

        override fun getItemCount(): Int {
            return listaCitas.size
        }
    }
