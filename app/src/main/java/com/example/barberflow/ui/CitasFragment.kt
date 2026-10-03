package com.example.barberflow.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Historial de citas del cliente (lo que antes era HistorialActivity). */
class CitasFragment : Fragment(R.layout.fragment_citas) {

    private val api by lazy { RetrofitClient.getApi(requireContext()) }
    private lateinit var recyclerView: RecyclerView
    private lateinit var textoVacio: TextView
    private lateinit var adaptador: CitasAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_citas)
        textoVacio = view.findViewById(R.id.tv_vacio_historial)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adaptador = CitasAdapter(emptyList()) { cita -> confirmarCancelacion(cita) }
        recyclerView.adapter = adaptador

        cargarCitas()
    }

    // Al volver a esta pestaña refrescamos la lista (por si se reservó o canceló algo)
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && view != null) cargarCitas()
    }

    private fun cargarCitas() {
        val preferencias = requireContext().getSharedPreferences("BarberFlowPrefs", Context.MODE_PRIVATE)
        val idCliente = preferencias.getInt("CLIENTE_ID", -1)

        if (idCliente == -1) {
            Toast.makeText(requireContext(), R.string.citas_error_usuario, Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val citas = api.obtenerCitas(idCliente)

                // Primero las próximas (la más cercana arriba); después el resto, de más reciente a más antigua
                val (activas, resto) = citas.partition {
                    it.estado == EstadoCita.PENDIENTE || it.estado == EstadoCita.CONFIRMADA || it.estado == null
                }
                val ordenadas = activas.sortedBy { it.fecha_hora } + resto.sortedByDescending { it.fecha_hora }

                adaptador.actualizarLista(ordenadas)
                textoVacio.visibility = if (ordenadas.isEmpty()) View.VISIBLE else View.GONE
                recyclerView.visibility = if (ordenadas.isEmpty()) View.GONE else View.VISIBLE
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                context?.let { Toast.makeText(it, R.string.citas_error_carga, Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun confirmarCancelacion(cita: Cita) {
        if (!EstadoCita.sePuedeCancelar(cita.estado)) {
            Toast.makeText(
                requireContext(),
                getString(R.string.citas_ya_estado, EstadoCita.etiqueta(cita.estado).lowercase()),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.citas_cancelar_titulo)
            .setMessage(
                getString(
                    R.string.citas_cancelar_mensaje,
                    cita.servicio.nombre,
                    cita.barbero.nombre,
                    formatearFechaHora(cita.fecha_hora)
                )
            )
            .setPositiveButton(R.string.citas_cancelar_si) { _, _ -> cancelarCita(cita) }
            .setNegativeButton(R.string.citas_volver, null)
            .show()
    }

    private fun cancelarCita(cita: Cita) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val respuesta = api.cambiarEstadoCita(cita.id, EstadoUpdate(EstadoCita.CANCELADA))
                if (respuesta.isSuccessful) {
                    Toast.makeText(requireContext(), R.string.citas_cancelada, Toast.LENGTH_SHORT).show()
                    cargarCitas()
                } else {
                    Toast.makeText(requireContext(), mensajeDeError(respuesta), Toast.LENGTH_LONG).show()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                context?.let { Toast.makeText(it, R.string.error_conexion_corto, Toast.LENGTH_SHORT).show() }
            }
        }
    }
}
