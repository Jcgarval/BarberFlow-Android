package com.example.barberflow.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.barberflow.R
import com.example.barberflow.api.RetrofitClient
import com.example.barberflow.models.Cita
import com.example.barberflow.models.aplicarBadgeEstado
import com.example.barberflow.models.diasHasta
import com.example.barberflow.models.esCitaProxima
import com.example.barberflow.models.formatearDiaLargo
import com.example.barberflow.models.formatearHora
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Pantalla de inicio del cliente: saludo, su próxima cita y accesos rápidos. */
class InicioFragment : Fragment(R.layout.fragment_inicio) {

    private val api by lazy { RetrofitClient.getApi(requireContext()) }
    private var jobCarga: Job? = null

    private lateinit var progreso: ProgressBar
    private lateinit var textoError: TextView
    private lateinit var tarjetaProxima: MaterialCardView
    private lateinit var tarjetaSinCita: MaterialCardView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progreso = view.findViewById(R.id.progress_inicio)
        textoError = view.findViewById(R.id.tv_inicio_error)
        tarjetaProxima = view.findViewById(R.id.card_proxima)
        tarjetaSinCita = view.findViewById(R.id.card_sin_cita)

        // Saludo con el primer nombre
        val preferencias = requireContext().getSharedPreferences("BarberFlowPrefs", Context.MODE_PRIVATE)
        val nombreCompleto = preferencias.getString("CLIENTE_NOMBRE", "")?.trim().orEmpty()
        val primerNombre = nombreCompleto.substringBefore(' ')
        view.findViewById<TextView>(R.id.tv_inicio_saludo).text =
            if (primerNombre.isBlank()) getString(R.string.home_saludo_sin_nombre)
            else getString(R.string.home_saludo, primerNombre)

        view.findViewById<View>(R.id.btn_inicio_reservar).setOnClickListener {
            (activity as? MainActivity)?.irA(R.id.nav_reservar)
        }
        view.findViewById<View>(R.id.btn_inicio_citas).setOnClickListener {
            (activity as? MainActivity)?.irA(R.id.nav_citas)
        }

        cargarProximaCita()
    }

    // Con show/hide, onResume no se repite al volver a esta pestaña: refrescamos aquí
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && view != null) cargarProximaCita()
    }

    private fun cargarProximaCita() {
        val preferencias = requireContext().getSharedPreferences("BarberFlowPrefs", Context.MODE_PRIVATE)
        val idCliente = preferencias.getInt("CLIENTE_ID", -1)
        if (idCliente == -1) return

        jobCarga?.cancel()
        progreso.visibility = View.VISIBLE
        textoError.visibility = View.GONE

        jobCarga = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val citas = api.obtenerCitas(idCliente)
                val proxima = citas.filter { esCitaProxima(it) }.minByOrNull { it.fecha_hora }

                progreso.visibility = View.GONE
                if (proxima == null) {
                    tarjetaProxima.visibility = View.GONE
                    tarjetaSinCita.visibility = View.VISIBLE
                } else {
                    tarjetaSinCita.visibility = View.GONE
                    mostrarProxima(proxima)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                progreso.visibility = View.GONE
                textoError.visibility = View.VISIBLE
            }
        }
    }

    private fun mostrarProxima(cita: Cita) {
        val raiz = requireView()
        raiz.findViewById<TextView>(R.id.tv_inicio_dia).text = formatearDiaLargo(cita.fecha_hora)
        raiz.findViewById<TextView>(R.id.tv_inicio_hora).text = formatearHora(cita.fecha_hora)
        raiz.findViewById<TextView>(R.id.tv_inicio_servicio).text = cita.servicio.nombre
        raiz.findViewById<TextView>(R.id.tv_inicio_barbero).text = getString(R.string.home_con_barbero, cita.barbero.nombre)
        aplicarBadgeEstado(raiz.findViewById(R.id.tv_inicio_estado), cita.estado)

        val etiquetaDias = raiz.findViewById<TextView>(R.id.tv_inicio_proximidad)
        val dias = diasHasta(cita.fecha_hora)
        when {
            dias == null || dias < 0 -> etiquetaDias.visibility = View.GONE
            else -> {
                etiquetaDias.visibility = View.VISIBLE
                etiquetaDias.text = when (dias) {
                    0 -> getString(R.string.home_hoy)
                    1 -> getString(R.string.home_manana)
                    else -> getString(R.string.home_en_dias, dias)
                }
            }
        }
        tarjetaProxima.visibility = View.VISIBLE
    }
}
