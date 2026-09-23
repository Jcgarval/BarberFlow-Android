package com.example.barberflow

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.Toast
import retrofit2.Retrofit

class HistorialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_citas)

        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("http://192.168.1.23:8000")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()

        val api = retrofit.create(BarberiaApi::class.java)

        lifecycleScope.launch {
            try {
                val citasReales = api.obtenerCitas()

                val adaptador = CitasAdapter(citasReales)
                recyclerView.adapter = adaptador

            } catch (e: Exception) {
                android.widget.Toast.makeText(this@HistorialActivity, "Fallo al descargar historial: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}