package com.example.barberflow.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barberflow.R
import com.example.barberflow.adapters.CitasAdapter
import com.example.barberflow.api.RetrofitClient
import com.example.barberflow.models.Cita
import com.example.barberflow.models.EstadoCita
import com.example.barberflow.models.EstadoUpdate
import com.example.barberflow.models.formatearFechaHora
import com.example.barberflow.models.mensajeDeError
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class HistorialActivity : AppCompatActivity() {

    private val api by lazy { RetrofitClient.getApi(this) }
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvVacio: TextView
    private lateinit var adaptador: CitasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial)

        recyclerView = findViewById(R.id.rv_citas)
        tvVacio = findViewById(R.id.tv_vacio_historial)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adaptador = CitasAdapter(emptyList()) { cita -> confirmarCancelacion(cita) }
        recyclerView.adapter = adaptador

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cargarCitas()
    }

    private fun cargarCitas() {
        val preferencias = getSharedPreferences("BarberFlowPrefs", MODE_PRIVATE)
        val idClienteActual = preferencias.getInt("CLIENTE_ID", -1)

        if (idClienteActual == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val citas = api.obtenerCitas(idClienteActual)

                // Primero las próximas (pendientes/confirmadas, la más cercana arriba); después el resto, de más reciente a más antigua
                val (activas, resto) = citas.partition {
                    it.estado == EstadoCita.PENDIENTE || it.estado == EstadoCita.CONFIRMADA || it.estado == null
                }
                val ordenadas = activas.sortedBy { it.fecha_hora } + resto.sortedByDescending { it.fecha_hora }

                adaptador.actualizarLista(ordenadas)
                tvVacio.visibility = if (ordenadas.isEmpty()) View.VISIBLE else View.GONE
                recyclerView.visibility = if (ordenadas.isEmpty()) View.GONE else View.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(this@HistorialActivity, "Fallo al descargar historial: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmarCancelacion(cita: Cita) {
        if (!EstadoCita.sePuedeCancelar(cita.estado)) {
            Toast.makeText(
                this,
                "Esta cita ya está ${EstadoCita.etiqueta(cita.estado).lowercase()}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Cancelar cita")
            .setMessage(
                "¿Quieres cancelar tu cita de ${cita.servicio.nombre} con ${cita.barbero.nombre} " +
                    "el ${formatearFechaHora(cita.fecha_hora)}?"
            )
            .setPositiveButton("Sí, cancelar") { _, _ -> cancelarCita(cita) }
            .setNegativeButton("Volver", null)
            .show()
    }

    private fun cancelarCita(cita: Cita) {
        lifecycleScope.launch {
            try {
                val respuesta = api.cambiarEstadoCita(cita.id, EstadoUpdate(EstadoCita.CANCELADA))
                if (respuesta.isSuccessful) {
                    Toast.makeText(this@HistorialActivity, "Cita cancelada", Toast.LENGTH_SHORT).show()
                    cargarCitas()
                } else {
                    Toast.makeText(this@HistorialActivity, mensajeDeError(respuesta), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@HistorialActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
