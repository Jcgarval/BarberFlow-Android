package com.example.barberflow.ui

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
import com.example.barberflow.R
import com.example.barberflow.adapters.AdminCitasAdapter
import com.example.barberflow.api.RetrofitClient
import com.example.barberflow.models.CitaDetalle
import com.example.barberflow.models.EstadoCita
import com.example.barberflow.models.EstadoUpdate
import com.example.barberflow.models.mensajeDeError
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class AdminCitasActivity : AppCompatActivity() {

    private val api by lazy { RetrofitClient.getApi(this) }
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

        findViewById<MaterialToolbar>(R.id.toolbar_agenda).setNavigationOnClickListener { finish() }

        rvCitas = findViewById(R.id.rv_citas)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        rvCitas.layoutManager = LinearLayoutManager(this)

        adapter = AdminCitasAdapter(
            emptyList(),
            onDeleteClick = { cita -> mostrarDialogoEliminarCita(cita) },
            onItemClick = { cita -> mostrarDialogoCambiarEstado(cita) }
        )
        rvCitas.adapter = adapter

        cargarCitas()
    }

    private fun cargarCitas() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            rvCitas.visibility = View.GONE
            tvEmptyState.visibility = View.GONE

            try {
                val listaCitas = api.obtenerCitasDetalladas()
                progressBar.visibility = View.GONE

                if (listaCitas.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                } else {
                    rvCitas.visibility = View.VISIBLE
                    adapter.actualizarLista(listaCitas)
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e("BarberFlow", "Error obteniendo citas: ", e)
                Toast.makeText(this@AdminCitasActivity, R.string.agenda_error_al_cargar_agenda, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------- Cambiar estado (tocar la tarjeta) ----------
    private fun mostrarDialogoCambiarEstado(cita: CitaDetalle) {
        val estadoActual = cita.estado ?: EstadoCita.PENDIENTE
        val opciones = EstadoCita.todos.map { EstadoCita.etiqueta(this@AdminCitasActivity, it) }.toTypedArray()
        val seleccionInicial = EstadoCita.todos.indexOf(estadoActual).coerceAtLeast(0)

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.agenda_estado_de_la_cita_de, cita.cliente_nombre))
            .setSingleChoiceItems(opciones, seleccionInicial) { dialog, which ->
                dialog.dismiss()
                val nuevoEstado = EstadoCita.todos[which]
                if (nuevoEstado != estadoActual) {
                    cambiarEstadoEnApi(cita.id, nuevoEstado)
                }
            }
            .setNegativeButton(R.string.comun_cerrar, null)
            .show()
    }

    private fun cambiarEstadoEnApi(id: Int, nuevoEstado: String) {
        lifecycleScope.launch {
            try {
                val respuesta = api.cambiarEstadoCita(id, EstadoUpdate(nuevoEstado))
                if (respuesta.isSuccessful) {
                    Toast.makeText(this@AdminCitasActivity, R.string.agenda_estado_actualizado, Toast.LENGTH_SHORT).show()
                    cargarCitas()
                } else {
                    // Por ejemplo: reactivar una cita cuyo hueco ya ocupó otro cliente
                    Toast.makeText(this@AdminCitasActivity, mensajeDeError(this@AdminCitasActivity, respuesta), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminCitasActivity, R.string.agenda_fallo_de_conexion, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------- Eliminar definitivamente (papelera) ----------
    private fun mostrarDialogoEliminarCita(cita: CitaDetalle) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.comun_eliminar_cita)
            .setMessage(
                getString(R.string.agenda_se_borrara_definitivamente_la_cita_de_si, cita.cliente_nombre, cita.servicio_nombre)
            )
            .setPositiveButton(R.string.comun_eliminar) { _, _ -> eliminarCitaEnApi(cita.id) }
            .setNegativeButton(R.string.comun_volver, null)
            .show()
    }

    private fun eliminarCitaEnApi(id: Int) {
        lifecycleScope.launch {
            try {
                val respuesta = api.eliminarCita(id)
                if (respuesta.isSuccessful) {
                    Toast.makeText(this@AdminCitasActivity, R.string.agenda_cita_eliminada, Toast.LENGTH_SHORT).show()
                    cargarCitas()
                } else {
                    Toast.makeText(this@AdminCitasActivity, mensajeDeError(this@AdminCitasActivity, respuesta), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminCitasActivity, getString(R.string.agenda_error_al_eliminar_la_cita, e.message), Toast.LENGTH_LONG).show()
            }
        }
    }
}
