package com.example.barberflow.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.example.barberflow.adapters.AdminCitasAdapter

class AdminCitasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_citas)

        // Ajuste para evitar el notch/cámara superior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val rvCitas = findViewById<RecyclerView>(R.id.rv_citas)
        rvCitas.layoutManager = LinearLayoutManager(this)

        val api = RetrofitClient.getApi(this)

        lifecycleScope.launch {
            try {
                // Llamamos a la nueva ruta que trae los nombres reales
                val listaCitas = api.obtenerCitasDetalladas()
                rvCitas.adapter = AdminCitasAdapter(listaCitas)
            } catch (e: Exception) {
                Log.e("BarberFlow", "Error obteniendo citas: ", e)
                Toast.makeText(this@AdminCitasActivity, "Error al cargar agenda", Toast.LENGTH_SHORT).show()
            }
        }
    }
}