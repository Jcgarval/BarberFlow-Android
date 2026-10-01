package com.example.barberflow.ui

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

// Importaciones de nuestro proyecto
import com.example.barberflow.R
import com.example.barberflow.api.RetrofitClient
import com.example.barberflow.adapters.CitasAdapter

class HistorialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial)

        val recyclerView = findViewById<RecyclerView>(R.id.rv_citas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // ⚠️ SOLUCIÓN: Usamos el cliente centralizado para inyectar el Token automáticamente
        val api = RetrofitClient.getApi(this)

        lifecycleScope.launch {
            try {
                // 1. Leemos el ID del cliente registrado desde la "caja fuerte"
                val preferencias = getSharedPreferences("BarberFlowPrefs", MODE_PRIVATE)
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
                                Toast.makeText(
                                    this@HistorialActivity,
                                    "Cita cancelada",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@HistorialActivity,
                                    "Error al cancelar",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@HistorialActivity,
                                "Fallo de conexión",
                                Toast.LENGTH_SHORT
                            ).show()
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