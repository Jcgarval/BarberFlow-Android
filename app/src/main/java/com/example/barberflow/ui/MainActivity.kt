package com.example.barberflow.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
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
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 1. Recuperamos los datos del cliente desde la memoria interna
        val preferencias = getSharedPreferences("BarberFlowPrefs", MODE_PRIVATE)
        val idClienteGuardado = preferencias.getInt("CLIENTE_ID", -1)
        val nombreClienteGuardado = preferencias.getString("CLIENTE_NOMBRE", "Cliente")

        // 2. Enlazamos la interfaz (¡Ya no existe inputCliente!)
        val boton = findViewById<Button>(R.id.btn_reservar)
        val inputBarbero = findViewById<Spinner>(R.id.spinner_barbero)
        val inputServicio = findViewById<Spinner>(R.id.spinner_servicio)
        val botonCitas = findViewById<Button>(R.id.btn_ver_historial)
        val textoFechaYHora = findViewById<TextView>(R.id.tv_FechaYHora)

        // Saludo personalizado opcional si tienes un TextView para el título
        // findViewById<TextView>(R.id.tv_titulo_reserva).text = "Reserva tu Cita, $nombreClienteGuardado"

        // 3. Configuramos Retrofit

        val api = RetrofitClient.getApi(this)

        var barberosReales: List<Barbero> = emptyList()
        var serviciosReales: List<Servicio> = emptyList()

        // 4. Descargamos datos para los desplegables
        lifecycleScope.launch {
            try {
                barberosReales = api.obtenerBarberos()
                serviciosReales = api.obtenerServicios()

                val nombresBarberos = barberosReales.map { it.nombre }
                val nombresServicios = serviciosReales.map { it.nombre }

                val adaptadorBarberos = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, nombresBarberos)
                inputBarbero.adapter = adaptadorBarberos

                val adaptadorServicios = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, nombresServicios)
                inputServicio.adapter = adaptadorServicios

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error al cargar datos del servidor", Toast.LENGTH_LONG).show()
            }
        }

        // 5. Selector de Fecha y Hora
        var fechaSeleccionadaParaBackend = ""

        textoFechaYHora.setOnClickListener {
            val calendarioActual = Calendar.getInstance()

            val selectorFecha = DatePickerDialog(this, { _, añoElegido, mesElegido, diaElegido ->

                // Comprobamos qué día de la semana ha elegido
                val fechaComprobacion = Calendar.getInstance()
                fechaComprobacion.set(añoElegido, mesElegido, diaElegido)

                // Si es domingo (DAY_OF_WEEK = 1), cortamos el proceso y avisamos
                if (fechaComprobacion.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    Toast.makeText(this, "Cerramos los domingos. Por favor, elige otro día.", Toast.LENGTH_LONG).show()
                    return@DatePickerDialog // Salimos del selector de fecha sin abrir la hora
                }

                val mesFormateado = String.format("%02d", mesElegido + 1)
                val diaFormateado = String.format("%02d", diaElegido)
                val fechaParcial = "$añoElegido-$mesFormateado-$diaFormateado"

                val selectorHora = TimePickerDialog(this, { _, horaElegida, minutoElegido ->

                    // Comprobamos el horario comercial (ej. de 09:00 a 19:59)
                    if (horaElegida < 9 || horaElegida >= 20) {
                        Toast.makeText(this, "Horario válido: de 09:00 a 20:00", Toast.LENGTH_LONG).show()
                        return@TimePickerDialog
                    }

                    val horaFormateada = String.format("%02d", horaElegida)
                    val minutoFormateado = String.format("%02d", minutoElegido)

                    fechaSeleccionadaParaBackend = "${fechaParcial}T${horaFormateada}:${minutoFormateado}:00"
                    val fechaVisual = "$diaFormateado/$mesFormateado/$añoElegido a las $horaFormateada:$minutoFormateado"
                    textoFechaYHora.text = fechaVisual

                }, calendarioActual.get(Calendar.HOUR_OF_DAY), calendarioActual.get(Calendar.MINUTE), true)

                selectorHora.show()

            }, calendarioActual.get(Calendar.YEAR), calendarioActual.get(Calendar.MONTH), calendarioActual.get(
                Calendar.DAY_OF_MONTH))

            // Bloqueamos las fechas pasadas. El usuario solo puede elegir desde hoy en adelante.
            selectorFecha.datePicker.minDate = System.currentTimeMillis()

            selectorFecha.show()
        }

        // 6. Botón de Reservar Cita usando el ID invisible
        boton.setOnClickListener {
            if (fechaSeleccionadaParaBackend.isEmpty()) {
                Toast.makeText(this, "¡Error! Elige una fecha", Toast.LENGTH_SHORT).show()
            } else if (barberosReales.isEmpty() || serviciosReales.isEmpty()) {
                Toast.makeText(this, "Espera a que carguen los datos", Toast.LENGTH_SHORT).show()
            } else if (idClienteGuardado == -1) {
                Toast.makeText(this, "Error crítico: Usuario no registrado", Toast.LENGTH_LONG).show()
            } else {
                val idBarberoReal = barberosReales[inputBarbero.selectedItemPosition].id
                val idServicioReal = serviciosReales[inputServicio.selectedItemPosition].id

                val citaDePrueba = Cita(
                    cliente_id = idClienteGuardado, // Aquí usamos el ID real invisible
                    barbero_id = idBarberoReal,
                    servicio_id = idServicioReal,
                    fecha_hora = fechaSeleccionadaParaBackend
                )

                api.crearCita(citaDePrueba).enqueue(object : Callback<Cita> {
                    override fun onResponse(call: Call<Cita>, response: Response<Cita>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@MainActivity, "¡Cita reservada con éxito, $nombreClienteGuardado!", Toast.LENGTH_SHORT).show()
                            textoFechaYHora.text = "Seleccionar Fecha y Hora"
                            fechaSeleccionadaParaBackend = ""
                        } else {
                            Toast.makeText(this@MainActivity, "Error del servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Cita>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Fallo de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        botonCitas.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        // Configuración del botón Cerrar Sesión
        val botonCerrarSesion = findViewById<Button>(R.id.btn_cerrar_sesion)
        botonCerrarSesion.setOnClickListener {
            // 1. Vaciamos las SharedPreferences
            preferencias.edit().clear().apply()

            // 2. Avisamos al usuario
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

            // 3. Lo devolvemos a la pantalla de Login y destruimos la actual
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}