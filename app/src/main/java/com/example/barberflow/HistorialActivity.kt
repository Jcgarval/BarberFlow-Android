package com.example.barberflow

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HistorialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial)

        val recyclerView = findViewById<RecyclerView>(R.id.rv_citas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Configuración de Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.23:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(BarberiaApi::class.java)

        lifecycleScope.launch {
            try {
                // 1. Leemos el ID del cliente registrado desde la "caja fuerte"
                val preferencias = getSharedPreferences("BarberFlowPrefs", Context.MODE_PRIVATE)
                val idClienteActual = preferencias.getInt("CLIENTE_ID", -1)

                // 2. Si no hay ID, mostramos error y detenemos la descarga
                if (idClienteActual == -1) {
                    Toast.makeText(this@HistorialActivity, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 3. Descargamos y convertimos la lista a MutableList para permitir borrados
                val citasReales = api.obtenerCitas(idClienteActual).toMutableList()

                // 4. Creamos el adaptador y definimos la acción de la papelera
                val adaptador = CitasAdapter(citasReales) { idCitaABorrar, posicionEnLista ->

                    // Lanzamos una petición a FastAPI para eliminar la cita de la base de datos
                    lifecycleScope.launch {
                        try {
                            val respuesta = api.eliminarCita(idCitaABorrar)
                            if (respuesta.isSuccessful) {
                                // Borramos la tarjeta visualmente sin recargar toda la pantalla
                                (recyclerView.adapter as CitasAdapter).eliminarItem(posicionEnLista)
                                Toast.makeText(this@HistorialActivity, "Cita cancelada", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@HistorialActivity, "Error al cancelar", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@HistorialActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                recyclerView.adapter = adaptador

            } catch (e: Exception) {
                Toast.makeText(this@HistorialActivity, "Fallo al descargar historial: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}