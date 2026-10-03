package com.example.barberflow.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.barberflow.R
import com.example.barberflow.api.RetrofitClient
import com.example.barberflow.models.Barbero
import com.example.barberflow.models.Cita
import com.example.barberflow.models.Servicio
import com.example.barberflow.models.mensajeDeError
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

class ReservaFragment : Fragment(R.layout.fragment_reserva) {

    private val api by lazy { RetrofitClient.getApi(requireContext()) }

    private var barberosReales: List<Barbero> = emptyList()
    private var serviciosReales: List<Servicio> = emptyList()

    private var fechaElegida: String? = null   // "yyyy-MM-dd"
    private var horaElegida: String? = null    // "HH:mm"
    private var jobFranjas: Job? = null        // para cancelar la consulta anterior si el usuario cambia de opción

    private lateinit var spinnerBarbero: Spinner
    private lateinit var spinnerServicio: Spinner
    private lateinit var textoFecha: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var progressFranjas: ProgressBar
    private lateinit var textoInfoFranjas: TextView
    private lateinit var botonReservar: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferencias = requireContext().getSharedPreferences("BarberFlowPrefs", Context.MODE_PRIVATE)
        val idCliente = preferencias.getInt("CLIENTE_ID", -1)
        val nombreCliente = preferencias.getString("CLIENTE_NOMBRE", "") ?: ""

        spinnerBarbero = view.findViewById(R.id.spinner_barbero)
        spinnerServicio = view.findViewById(R.id.spinner_servicio)
        textoFecha = view.findViewById(R.id.tv_FechaYHora)
        chipGroup = view.findViewById(R.id.chip_group_franjas)
        progressFranjas = view.findViewById(R.id.progress_franjas)
        textoInfoFranjas = view.findViewById(R.id.tv_franjas_info)
        botonReservar = view.findViewById(R.id.btn_reservar)

        // Al cambiar de barbero o de servicio, las horas libres cambian: las volvemos a pedir
        val alCambiarSeleccion = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                cargarFranjas()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerBarbero.onItemSelectedListener = alCambiarSeleccion
        spinnerServicio.onItemSelectedListener = alCambiarSeleccion

        cargarBarberosYServicios()

        textoFecha.setOnClickListener { abrirSelectorDeFecha() }
        botonReservar.setOnClickListener { reservar(idCliente, nombreCliente) }
    }

    private fun cargarBarberosYServicios() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                barberosReales = api.obtenerBarberos()
                serviciosReales = api.obtenerServicios()

                spinnerBarbero.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    barberosReales.map { it.nombre }
                )
                spinnerServicio.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    serviciosReales.map { it.nombre }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                context?.let { Toast.makeText(it, R.string.reserva_error_datos, Toast.LENGTH_LONG).show() }
            }
        }
    }

    private fun abrirSelectorDeFecha() {
        val hoy = Calendar.getInstance()

        val selector = DatePickerDialog(requireContext(), { _, anio, mes, dia ->
            val comprobacion = Calendar.getInstance()
            comprobacion.set(anio, mes, dia)

            // Cerramos los domingos
            if (comprobacion.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                Toast.makeText(requireContext(), R.string.reserva_domingos, Toast.LENGTH_LONG).show()
                return@DatePickerDialog
            }

            fechaElegida = String.format(Locale.US, "%04d-%02d-%02d", anio, mes + 1, dia)
            textoFecha.text = String.format(Locale.US, "%02d/%02d/%04d", dia, mes + 1, anio)
            cargarFranjas()

        }, hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH))

        // Solo se puede elegir desde hoy en adelante
        selector.datePicker.minDate = System.currentTimeMillis()
        selector.show()
    }

    /** Pide al servidor las horas libres para el barbero, servicio y día elegidos y las pinta como chips. */
    private fun cargarFranjas() {
        val fecha = fechaElegida ?: return
        if (barberosReales.isEmpty() || serviciosReales.isEmpty()) return

        val barbero = barberosReales.getOrNull(spinnerBarbero.selectedItemPosition) ?: return
        val servicio = serviciosReales.getOrNull(spinnerServicio.selectedItemPosition) ?: return

        jobFranjas?.cancel()
        horaElegida = null
        chipGroup.removeAllViews()
        chipGroup.visibility = View.GONE
        textoInfoFranjas.visibility = View.GONE
        progressFranjas.visibility = View.VISIBLE

        jobFranjas = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val respuesta = api.obtenerDisponibilidad(barbero.id, servicio.id, fecha)
                progressFranjas.visibility = View.GONE

                if (respuesta.franjas.isEmpty()) {
                    textoInfoFranjas.setText(R.string.reserva_sin_franjas)
                    textoInfoFranjas.visibility = View.VISIBLE
                } else {
                    textoInfoFranjas.text = getString(R.string.reserva_info_franjas, servicio.nombre, respuesta.duracion_minutos)
                    textoInfoFranjas.visibility = View.VISIBLE
                    respuesta.franjas.forEach { hora -> chipGroup.addView(crearChip(hora)) }
                    chipGroup.visibility = View.VISIBLE
                }
            } catch (e: CancellationException) {
                throw e  // el usuario cambió de opción: esta consulta ya no importa
            } catch (e: HttpException) {
                progressFranjas.visibility = View.GONE
                val mensaje = if (e.code() == 401) {
                    getString(R.string.reserva_sesion_caducada)
                } else {
                    getString(R.string.reserva_error_franjas, e.code())
                }
                context?.let { Toast.makeText(it, mensaje, Toast.LENGTH_LONG).show() }
            } catch (e: Exception) {
                progressFranjas.visibility = View.GONE
                context?.let { Toast.makeText(it, R.string.reserva_error_conexion_franjas, Toast.LENGTH_LONG).show() }
            }
        }
    }

    private fun crearChip(hora: String): Chip {
        val chip = layoutInflater.inflate(R.layout.item_chip_franja, chipGroup, false) as Chip
        chip.id = View.generateViewId()
        chip.text = hora
        chip.setOnCheckedChangeListener { _, marcado ->
            if (marcado) {
                horaElegida = hora
            } else if (horaElegida == hora) {
                horaElegida = null
            }
        }
        return chip
    }

    private fun reservar(idCliente: Int, nombreCliente: String) {
        val fecha = fechaElegida
        val hora = horaElegida

        when {
            fecha == null -> toast(R.string.reserva_elige_dia)
            hora == null -> toast(R.string.reserva_elige_hora)
            barberosReales.isEmpty() || serviciosReales.isEmpty() -> toast(R.string.reserva_espera_datos)
            idCliente == -1 -> toast(R.string.reserva_error_usuario)
            else -> {
                val nuevaCita = Cita(
                    cliente_id = idCliente,
                    barbero_id = barberosReales[spinnerBarbero.selectedItemPosition].id,
                    servicio_id = serviciosReales[spinnerServicio.selectedItemPosition].id,
                    fecha_hora = "${fecha}T${hora}:00"
                )

                botonReservar.isEnabled = false  // evita doble toque mientras se envía
                api.crearCita(nuevaCita).enqueue(object : Callback<Cita> {
                    override fun onResponse(call: Call<Cita>, response: Response<Cita>) {
                        if (!isAdded) return
                        botonReservar.isEnabled = true
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), getString(R.string.reserva_ok, nombreCliente), Toast.LENGTH_SHORT).show()
                            limpiarSeleccionDeFecha()
                            // Llevamos al usuario a Inicio para que vea su cita como "próxima cita"
                            (activity as? MainActivity)?.irA(R.id.nav_inicio)
                        } else {
                            Toast.makeText(requireContext(), mensajeDeError(response), Toast.LENGTH_LONG).show()
                            // Si la hora se ocupó mientras elegías, refrescamos las franjas
                            if (response.code() == 400) cargarFranjas()
                        }
                    }

                    override fun onFailure(call: Call<Cita>, t: Throwable) {
                        if (!isAdded) return
                        botonReservar.isEnabled = true
                        Toast.makeText(requireContext(), getString(R.string.reserva_error_conexion, t.message ?: ""), Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    private fun toast(textoId: Int) {
        Toast.makeText(requireContext(), textoId, Toast.LENGTH_SHORT).show()
    }

    private fun limpiarSeleccionDeFecha() {
        jobFranjas?.cancel()
        fechaElegida = null
        horaElegida = null
        textoFecha.setText(R.string.reserva_seleccionar_fecha)
        chipGroup.removeAllViews()
        chipGroup.visibility = View.GONE
        textoInfoFranjas.visibility = View.GONE
        progressFranjas.visibility = View.GONE
    }
}
