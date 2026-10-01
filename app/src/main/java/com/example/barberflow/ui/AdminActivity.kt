package com.example.barberflow.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Importaciones de nuestro proyecto
import com.example.barberflow.R

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // 1. Buscamos los 4 botones exactos que pusimos en el nuevo XML
        val btnGestionarBarberos = findViewById<Button>(R.id.btnGestionarBarberos)
        val btnGestionarServicios = findViewById<Button>(R.id.btnGestionarServicios)
        val btnVerAgenda = findViewById<Button>(R.id.btnVerAgenda)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)

        // 2. Acción: Navegar a la pantalla de Barberos
        btnGestionarBarberos.setOnClickListener {
            val intent = Intent(this, GestionBarberosActivity::class.java)
            startActivity(intent)
        }

        // 3. Acción: Navegar a la pantalla de Servicios
        btnGestionarServicios.setOnClickListener {
            val intent = Intent(this, GestionServiciosActivity::class.java)
            startActivity(intent)
        }

        // 4. Acción: Ver Agenda (Conservamos tu lógica original)
        btnVerAgenda.setOnClickListener {
            val intent = Intent(this, AdminCitasActivity::class.java)
            startActivity(intent)
        }

        // 5. Acción: Cerrar Sesión (Conservamos tu lógica original segura)
        btnCerrarSesion.setOnClickListener {
            val preferencias = getSharedPreferences("BarberFlowPrefs", MODE_PRIVATE)
            preferencias.edit().clear().apply()

            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Cierra esta pantalla para que no puedan volver atrás con el botón del móvil
        }
    }
}