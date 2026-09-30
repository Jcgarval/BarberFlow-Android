package com.example.barberflow

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class AdminCitasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_citas)

        // Ajuste para evitar el notch/cámara superior
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val rvCitas = findViewById<RecyclerView>(R.id.rv_citas)
        rvCitas.layoutManager = LinearLayoutManager(this)

        val api = RetrofitClient.getApi(this)

        lifecycleScope.launch {
            try {
                // Llamamos a la nueva ruta que trae los nombres reales
                val listaCitas = api.obtenerCitasDetalladas()
                rvCitas.adapter = AdminCitasAdapter(listaCitas)
            } catch (e: Exception) {
                Log.e("BarberFlow", "Error obteniendo citas: ", e)
                Toast.makeText(this@AdminCitasActivity, "Error al cargar agenda", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// El adaptador ahora recibe una lista de CitaDetalle en vez de Cita
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