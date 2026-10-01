package com.example.barberflow.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

// Importaciones de nuestro proyecto
import com.example.barberflow.R
import com.example.barberflow.api.RetrofitClient
import com.example.barberflow.adapters.AdminCitasAdapter
import com.example.barberflow.models.CitaDetalle

class AdminCitasActivity : AppCompatActivity() {

    private lateinit var rvCitas: RecyclerView
    private lateinit var adapter: AdminCitasAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_citas)

        // Ajuste para evitar el notch/cámara superior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvCitas = findViewById(R.id.rv_citas)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        rvCitas.layoutManager = LinearLayoutManager(this)

        adapter = AdminCitasAdapter(emptyList()) { cita ->
            mostrarDialogoEliminarCita(cita)
        }
        rvCitas.adapter = adapter

        cargarCitas()
    }

    private fun cargarCitas() {
        val api = RetrofitClient.getApi(this)

        lifecycleScope.launch {
            // 1. Mostramos la rueda de carga y ocultamos la lista antes de la petición
            progressBar.visibility = View.VISIBLE
            rvCitas.visibility = View.GONE
            tvEmptyState.visibility = View.GONE

            try {
                val listaCitas = api.obtenerCitasDetalladas()

                // 2. Petición terminada, ocultamos la rueda
                progressBar.visibility = View.GONE

                // 3. Evaluamos si la lista está vacía para mostrar un elemento u otro
                if (listaCitas.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                } else {
                    rvCitas.visibility = View.VISIBLE
                    adapter.actualizarLista(listaCitas)
                }
            } catch (e: Exception) {
                // En caso de error, ocultamos la rueda
                progressBar.visibility = View.GONE
                Log.e("BarberFlow", "Error obteniendo citas: ", e)
                Toast.makeText(this@AdminCitasActivity, "Error al cargar agenda", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoEliminarCita(cita: CitaDetalle) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cancelar Cita")
        builder.setMessage("¿Estás seguro de que quieres cancelar la cita de ${cita.cliente_nombre} para el servicio '${cita.servicio_nombre}'?")

        builder.setPositiveButton("Sí, cancelar") { dialog, _ ->
            eliminarCitaEnApi(cita.id)
            dialog.dismiss()
        }

        builder.setNegativeButton("Volver") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun eliminarCitaEnApi(id: Int) {
        val api = RetrofitClient.getApi(this)

        lifecycleScope.launch {
            try {
                api.eliminarCita(id)
                Toast.makeText(this@AdminCitasActivity, "Cita cancelada con éxito", Toast.LENGTH_SHORT).show()
                // Al recargar, volverá a mostrar la rueda y comprobará si la lista se quedó vacía
                cargarCitas()
            } catch (e: Exception) {
                Toast.makeText(this@AdminCitasActivity, "Error al cancelar la cita: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}