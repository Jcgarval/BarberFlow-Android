package com.example.barberflow.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

class MainActivity : AppCompatActivity() {

    private val api by lazy { RetrofitClient.getApi(this) }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 1. Datos del cliente guardados al iniciar sesión
        val preferencias = getSharedPreferences("BarberFlowPrefs", MODE_PRIVATE)
        val idClienteGuardado = preferencias.getInt("CLIENTE_ID", -1)
        val nombreClienteGuardado = preferencias.getString("CLIENTE_NOMBRE", "Cliente")

        // 2. Enlazamos la interfaz
        val botonReservar = findViewById<Button>(R.id.btn_reservar)
        val botonCitas = findViewById<Button>(R.id.btn_ver_historial)
        val botonCerrarSesion = findViewById<Button>(R.id.btn_cerrar_sesion)
        spinnerBarbero = findViewById(R.id.spinner_barbero)
        spinnerServicio = findViewById(R.id.spinner_servicio)
        textoFecha = findViewById(R.id.tv_FechaYHora)
        chipGroup = findViewById(R.id.chip_group_franjas)
        progressFranjas = findViewById(R.id.progress_franjas)
        textoInfoFranjas = findViewById(R.id.tv_franjas_info)

        // 3. Al cambiar de barbero o de servicio, las horas libres cambian: las volvemos a pedir
        val alCambiarSeleccion = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                cargarFranjas()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerBarbero.onItemSelectedListener = alCambiarSeleccion
        spinnerServicio.onItemSelectedListener = alCambiarSeleccion

        // 4. Descargamos barberos y servicios para los desplegables
        lifecycleScope.launch {
            try {
                barberosReales = api.obtenerBarberos()
                serviciosReales = api.obtenerServicios()

                spinnerBarbero.adapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    barberosReales.map { it.nombre }
                )
                spinnerServicio.adapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    serviciosReales.map { it.nombre }
                )
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error al cargar datos del servidor", Toast.LENGTH_LONG).show()
            }
        }

        // 5. Selector de DÍA (la hora se elige después entre las franjas libres)
        textoFecha.setOnClickListener {
            val hoy = Calendar.getInstance()

            val selectorFecha = DatePickerDialog(this, { _, anio, mes, dia ->
                val comprobacion = Calendar.getInstance()
                comprobacion.set(anio, mes, dia)

                // Cerramos los domingos
                if (comprobacion.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    Toast.makeText(this, "Cerramos los domingos. Por favor, elige otro día.", Toast.LENGTH_LONG).show()
                    return@DatePickerDialog
                }

                fechaElegida = String.format(Locale.US, "%04d-%02d-%02d", anio, mes + 1, dia)
                textoFecha.text = String.format(Locale.US, "%02d/%02d/%04d", dia, mes + 1, anio)
                cargarFranjas()

            }, hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH))

            // Solo se puede elegir desde hoy en adelante
            selectorFecha.datePicker.minDate = System.currentTimeMillis()
            selectorFecha.show()
        }

        // 6. Reservar
        botonReservar.setOnClickListener {
            val fecha = fechaElegida
            val hora = horaElegida

            when {
                fecha == null -> Toast.makeText(this, "Elige primero un día", Toast.LENGTH_SHORT).show()
                hora == null -> Toast.makeText(this, "Elige una hora libre", Toast.LENGTH_SHORT).show()
                barberosReales.isEmpty() || serviciosReales.isEmpty() ->
                    Toast.makeText(this, "Espera a que carguen los datos", Toast.LENGTH_SHORT).show()
                idClienteGuardado == -1 ->
                    Toast.makeText(this, "Error crítico: Usuario no registrado", Toast.LENGTH_LONG).show()
                else -> {
                    val nuevaCita = Cita(
                        cliente_id = idClienteGuardado,
                        barbero_id = barberosReales[spinnerBarbero.selectedItemPosition].id,
                        servicio_id = serviciosReales[spinnerServicio.selectedItemPosition].id,
                        fecha_hora = "${fecha}T${hora}:00"
                    )

                    botonReservar.isEnabled = false  // evita doble toque mientras se envía
                    api.crearCita(nuevaCita).enqueue(object : Callback<Cita> {
                        override fun onResponse(call: Call<Cita>, response: Response<Cita>) {
                            botonReservar.isEnabled = true
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "¡Cita reservada con éxito, $nombreClienteGuardado!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                limpiarSeleccionDeFecha()
                            } else {
                                Toast.makeText(this@MainActivity, mensajeDeError(response), Toast.LENGTH_LONG).show()
                                // Si la hora se ocupó mientras elegías, refrescamos las franjas
                                if (response.code() == 400) cargarFranjas()
                            }
                        }

                        override fun onFailure(call: Call<Cita>, t: Throwable) {
                            botonReservar.isEnabled = true
                            Toast.makeText(this@MainActivity, "Fallo de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }

        botonCitas.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        botonCerrarSesion.setOnClickListener {
            preferencias.edit().clear().apply()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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

        jobFranjas = lifecycleScope.launch {
            try {
                val respuesta = api.obtenerDisponibilidad(barbero.id, servicio.id, fecha)
                progressFranjas.visibility = View.GONE

                if (respuesta.franjas.isEmpty()) {
                    textoInfoFranjas.text = "No quedan horas libres ese día. Prueba con otro día, barbero o servicio."
                    textoInfoFranjas.visibility = View.VISIBLE
                } else {
                    textoInfoFranjas.text = "Elige una hora (${servicio.nombre}, ${respuesta.duracion_minutos} min)"
                    textoInfoFranjas.visibility = View.VISIBLE
                    respuesta.franjas.forEach { hora -> chipGroup.addView(crearChip(hora)) }
                    chipGroup.visibility = View.VISIBLE
                }
            } catch (e: CancellationException) {
                throw e  // el usuario cambió de opción: esta consulta ya no importa
            } catch (e: HttpException) {
                progressFranjas.visibility = View.GONE
                val mensaje = if (e.code() == 401) {
                    "Tu sesión ha caducado. Vuelve a iniciar sesión."
                } else {
                    "No se pudieron cargar las horas (error ${e.code()})"
                }
                Toast.makeText(this@MainActivity, mensaje, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                progressFranjas.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Fallo de conexión al cargar las horas", Toast.LENGTH_LONG).show()
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

    private fun limpiarSeleccionDeFecha() {
        jobFranjas?.cancel()
        fechaElegida = null
        horaElegida = null
        textoFecha.text = "Seleccionar fecha"
        chipGroup.removeAllViews()
        chipGroup.visibility = View.GONE
        textoInfoFranjas.visibility = View.GONE
        progressFranjas.visibility = View.GONE
    }
}
