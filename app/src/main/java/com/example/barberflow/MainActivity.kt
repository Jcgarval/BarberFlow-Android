package com.example.barberflow

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 1. Enlazamos la interfaz
        val boton = findViewById<Button>(R.id.btn_reservar)
        val inputCliente = findViewById<EditText>(R.id.et_cliente)
        val inputBarbero = findViewById<Spinner>(R.id.spinner_barbero)
        val inputServicio = findViewById<Spinner>(R.id.spinner_servicio)
        val botonCitas = findViewById<Button>(R.id.btn_ver_historial)
        val textoFechaYHora = findViewById<TextView>(R.id.tv_FechaYHora)

        // 2. Configuramos Retrofit (¡Asegúrate de que esta es tu IP actual!)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.23:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(BarberiaApi::class.java)

        // Variables para guardar los objetos reales descargados
        var barberosReales: List<Barbero> = emptyList()
        var serviciosReales: List<Servicio> = emptyList()

        // 3. Descargamos los datos dinámicos en segundo plano
        lifecycleScope.launch {
            try {
                barberosReales = api.obtenerBarberos()
                serviciosReales = api.obtenerServicios()

                // Extraemos solo los nombres para los desplegables
                val nombresBarberos = barberosReales.map { it.nombre }
                val nombresServicios = serviciosReales.map { it.nombre }

                // Rellenamos los Spinners
                val adaptadorBarberos = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, nombresBarberos)
                inputBarbero.adapter = adaptadorBarberos

                val adaptadorServicios = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, nombresServicios)
                inputServicio.adapter = adaptadorServicios

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error al cargar datos del servidor", Toast.LENGTH_LONG).show()
            }
        }

        // 4. Selector de Fecha y Hora
        var fechaSeleccionadaParaBackend = ""

        textoFechaYHora.setOnClickListener {
            val calendario = java.util.Calendar.getInstance()
            val anio = calendario.get(java.util.Calendar.YEAR)
            val mes = calendario.get(java.util.Calendar.MONTH)
            val dia = calendario.get(java.util.Calendar.DAY_OF_MONTH)

            val selectorFecha = android.app.DatePickerDialog(this, { _, añoElegido, mesElegido, diaElegido ->
                val mesReal = mesElegido + 1
                val mesFormateado = String.format("%02d", mesReal)
                val diaFormateado = String.format("%02d", diaElegido)
                val fechaParcial = "$añoElegido-$mesFormateado-$diaFormateado"

                val horaActual = calendario.get(java.util.Calendar.HOUR_OF_DAY)
                val minutoActual = calendario.get(java.util.Calendar.MINUTE)

                val selectorHora = android.app.TimePickerDialog(this, { _, horaElegida, minutoElegido ->
                    val horaFormateada = String.format("%02d", horaElegida)
                    val minutoFormateado = String.format("%02d", minutoElegido)

                    fechaSeleccionadaParaBackend = "${fechaParcial}T$horaFormateada:$minutoFormateado:00"
                    textoFechaYHora.text = fechaSeleccionadaParaBackend
                }, horaActual, minutoActual, true)

                selectorHora.show()
            }, anio, mes, dia)

            selectorFecha.show()
        }

        // 5. Botón de Reservar Cita
        boton.setOnClickListener {
            val textoCliente = inputCliente.text.toString()

            if (textoCliente.isEmpty() || fechaSeleccionadaParaBackend.isEmpty()) {
                Toast.makeText(this, "¡Error! Rellena el cliente y elige fecha", Toast.LENGTH_SHORT).show()
            } else if (barberosReales.isEmpty() || serviciosReales.isEmpty()) {
                Toast.makeText(this, "Espera a que carguen los datos del servidor", Toast.LENGTH_SHORT).show()
            } else {
                val numeroCliente = textoCliente.toInt()

                // Cogemos la posición elegida y sacamos el ID real de la lista descargada
                val posicionBarbero = inputBarbero.selectedItemPosition
                val idBarberoReal = barberosReales[posicionBarbero].id

                val posicionServicio = inputServicio.selectedItemPosition
                val idServicioReal = serviciosReales[posicionServicio].id

                val citaDePrueba = Cita(
                    cliente_id = numeroCliente,
                    barbero_id = idBarberoReal,
                    servicio_id = idServicioReal,
                    fecha_hora = fechaSeleccionadaParaBackend
                )

                api.crearCita(citaDePrueba).enqueue(object : retrofit2.Callback<Cita> {
                    override fun onResponse(call: retrofit2.Call<Cita>, response: retrofit2.Response<Cita>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@MainActivity, "¡Cita reservada con éxito!", Toast.LENGTH_SHORT).show()
                            inputCliente.text.clear()
                            textoFechaYHora.text = "Seleccionar Fecha y Hora"
                            fechaSeleccionadaParaBackend = ""
                        } else {
                            Toast.makeText(this@MainActivity, "Error del servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<Cita>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Fallo de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        // 6. Botón para ir al Historial
        botonCitas.setOnClickListener {
            val intent = android.content.Intent(this, HistorialActivity::class.java)
            startActivity(intent)
        }

        // 7. Configuración de márgenes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}