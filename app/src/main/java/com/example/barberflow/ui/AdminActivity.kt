package com.example.barberflow.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

// Importaciones de nuestro proyecto
import com.example.barberflow.R
import com.example.barberflow.api.RetrofitClient

class AdminActivity : AppCompatActivity() {

    // Declaramos los Textos del dashboard arriba para poder actualizarlos luego
    private lateinit var tvTotalCitas: TextView
    private lateinit var tvTotalBarberos: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        // Ajuste para evitar el notch y la barra de estado superior
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Vinculamos los nuevos textos del Dashboard
        tvTotalCitas = findViewById(R.id.tvTotalCitas)
        tvTotalBarberos = findViewById(R.id.tvTotalBarberos)

        // 2. Buscamos los botones
        val btnGestionarBarberos = findViewById<Button>(R.id.btnGestionarBarberos)
        val btnGestionarServicios = findViewById<Button>(R.id.btnGestionarServicios)
        val btnVerAgenda = findViewById<Button>(R.id.btnVerAgenda)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)

        // 3. Configuración de navegación
        btnGestionarBarberos.setOnClickListener {
            startActivity(Intent(this, GestionBarberosActivity::class.java))
        }

        btnGestionarServicios.setOnClickListener {
            startActivity(Intent(this, GestionServiciosActivity::class.java))
        }

        btnVerAgenda.setOnClickListener {
            startActivity(Intent(this, AdminCitasActivity::class.java))
        }

        // 4. Cerrar Sesión
        btnCerrarSesion.setOnClickListener {
            val preferencias = getSharedPreferences("BarberFlowPrefs", MODE_PRIVATE)
            preferencias.edit().clear().apply()

            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // onResume se ejecuta cada vez que el usuario vuelve a esta pantalla (ej. usando el botón atrás)
    override fun onResume() {
        super.onResume()
        cargarDatosDashboard()
    }

    private fun cargarDatosDashboard() {
        val api = RetrofitClient.getApi(this)

        lifecycleScope.launch {
            try {
                // Mientras carga, ponemos unos puntos suspensivos visuales
                tvTotalCitas.text = "..."
                tvTotalBarberos.text = "..."

                // Llamamos a los endpoints (usamos obtenerCitasDetalladas que ya vimos que existe)
                val citas = api.obtenerCitasDetalladas()
                tvTotalCitas.text = citas.size.toString()

                // Asumo que tu método para los barberos se llama obtenerBarberos() en BarberiaApi
                // Si el nombre es diferente, solo tienes que cambiar "obtenerBarberos()" por el tuyo.
                val barberos = api.obtenerBarberos()
                tvTotalBarberos.text = barberos.size.toString()

            } catch (e: Exception) {
                Log.e("BarberFlow", "Error al cargar datos del dashboard", e)
                // Si hay un fallo de conexión, mostramos un guión
                tvTotalCitas.text = "-"
                tvTotalBarberos.text = "-"
            }
        }
    }
}