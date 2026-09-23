package com.example.barberflow

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        // 1. Enlazamos la interfaz
        val boton = findViewById<Button>(R.id.btn_reservar)
        val titulo = findViewById<TextView>(R.id.tv_titulo)
        val inputCliente = findViewById<EditText>(R.id.et_cliente)
        val inputBarbero = findViewById<Spinner>(R.id.spinner_barbero)
        val inputServicio = findViewById<Spinner>(R.id.spinner_servicio)
        val botonCitas = findViewById<Button>(R.id.btn_ver_historial)

        val listaBarberos = listOf("Alejandro (ID: 1)", "María (ID: 2)", "Carlos (ID:3)")
        val listaServicios = listOf("Corte básico (ID: 1)", "Arreglo de barba (ID: 2)", "Tinte (ID: 3)")

        val adaptadorBarberos = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaBarberos)
        val adaptadorServicios = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaServicios)

        inputBarbero.adapter = adaptadorBarberos
        inputServicio.adapter = adaptadorServicios

        // 2. Configuramos Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.23:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(BarberiaApi::class.java)

        val textoFechaYHora = findViewById<TextView>(R.id.tv_FechaYHora)

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

                // 1. Guardamos la parte de la fecha
                val fechaParcial = "$añoElegido-$mesFormateado-$diaFormateado"

                // 2. Justo al elegir la fecha, preparamos el selector de hora
                val horaActual = calendario.get(java.util.Calendar.HOUR_OF_DAY)
                val minutoActual = calendario.get(java.util.Calendar.MINUTE)

                val selectorHora = android.app.TimePickerDialog(this, { _, horaElegida, minutoElegido ->

                    val horaFormateada = String.format("%02d", horaElegida)
                    val minutoFormateado = String.format("%02d", minutoElegido)

                    // 3. Lo unimos TODO: Fecha + "T" + Hora + Minutos + Segundos (00)
                    fechaSeleccionadaParaBackend = "${fechaParcial}T$horaFormateada:$minutoFormateado:00"

                    // 4. Mostramos el resultado final en la pantalla
                    textoFechaYHora.text = fechaSeleccionadaParaBackend

                }, horaActual, minutoActual, true) // "true" para usar formato de 24 horas

                // Mostramos el reloj en cuanto se acepta la fecha
                selectorHora.show()

            }, anio, mes, dia)

            selectorFecha.show()
        }


        // 3. Le decimos al botón qué hacer al pulsarlo
        boton.setOnClickListener {

            val textoCliente = inputCliente.text.toString()

            if (textoCliente.isEmpty() || fechaSeleccionadaParaBackend.isEmpty()) {
                android.widget.Toast.makeText(this, "¡Error! Rellena el cliente y elige fecha", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                val numeroCliente = textoCliente.toInt()

                val posicionBarbero = inputBarbero.selectedItemPosition
                val idBarbero = posicionBarbero + 1

                val posicionServicio = inputServicio.selectedItemPosition
                val idServicio = posicionServicio + 1

                val citaDePrueba = Cita(
                    cliente_id = numeroCliente,
                    barbero_id = idBarbero,
                    servicio_id = idServicio,
                    fecha_hora = fechaSeleccionadaParaBackend
                )

                api.crearCita(citaDePrueba).enqueue(object : retrofit2.Callback<Cita> {
                    override fun onResponse(
                        call: retrofit2.Call<Cita>,
                        response: retrofit2.Response<Cita>
                    ) {
                        if (response.isSuccessful) {
                            android.widget.Toast.makeText(this@MainActivity, "¡Cita creada en FastAPI!", android.widget.Toast.LENGTH_SHORT).show()
                            inputCliente.text.clear()
                            //inputBarbero.text.clear()
                            //inputServicio.text.clear()
                            textoFechaYHora.text = "Seleccionar Fecha y Hora"
                            fechaSeleccionadaParaBackend = ""
                        } else {
                            android.widget.Toast.makeText(this@MainActivity, "¡Error del servidor: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<Cita>, t: Throwable) {
                        android.widget.Toast.makeText(this@MainActivity, "Fallo: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        botonCitas.setOnClickListener {
            val intent = android.content.Intent(this, HistorialActivity::class.java)
            startActivity(intent)
        }
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }