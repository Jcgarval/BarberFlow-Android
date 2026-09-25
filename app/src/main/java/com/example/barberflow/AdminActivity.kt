package com.example.barberflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Referencias de Barbero
        val etNombreBarbero = findViewById<EditText>(R.id.et_nombre_barbero)
        val btnGuardarBarbero = findViewById<Button>(R.id.btn_guardar_barbero)

        // Referencias de Servicio
        val etNombreServicio = findViewById<EditText>(R.id.et_nombre_servicio)
        val etDuracionServicio = findViewById<EditText>(R.id.et_duracion_servicio)
        val etPrecioServicio = findViewById<EditText>(R.id.et_precio_servicio)
        val btnGuardarServicio = findViewById<Button>(R.id.btn_guardar_servicio)

        val btnCerrarSesion = findViewById<Button>(R.id.btn_cerrar_sesion)

        // Instancia centralizada de Retrofit con el Token inyectado
        val api = RetrofitClient.getApi(this)

        // Acción: Añadir Barbero
        btnGuardarBarbero.setOnClickListener {
            val nombre = etNombreBarbero.text.toString().trim()

            if (nombre.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        val peticion = BarberoCreate(nombre)
                        val respuesta = api.crearBarbero(peticion)

                        Toast.makeText(this@AdminActivity, "Barbero ${respuesta.nombre} creado con éxito", Toast.LENGTH_SHORT).show()
                        etNombreBarbero.text.clear() // Limpiamos el campo
                    } catch (e: Exception) {
                        Toast.makeText(this@AdminActivity, "Error al crear barbero: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "El nombre del barbero no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        // Acción: Añadir Servicio
        btnGuardarServicio.setOnClickListener {
            val nombre = etNombreServicio.text.toString().trim()
            val duracion = etDuracionServicio.text.toString().toIntOrNull()
            val precio = etPrecioServicio.text.toString().toDoubleOrNull()

            if (nombre.isNotEmpty() && duracion != null && precio != null) {
                lifecycleScope.launch {
                    try {
                        val peticion = ServicioCreate(nombre, duracion, precio)
                        val respuesta = api.crearServicio(peticion)

                        Toast.makeText(this@AdminActivity, "Servicio ${respuesta.nombre} creado con éxito", Toast.LENGTH_SHORT).show()

                        // Limpiamos los campos
                        etNombreServicio.text.clear()
                        etDuracionServicio.text.clear()
                        etPrecioServicio.text.clear()
                    } catch (e: Exception) {
                        Toast.makeText(this@AdminActivity, "Error al crear servicio: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Rellena todos los campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }

        // Acción: Cerrar Sesión
        btnCerrarSesion.setOnClickListener {
            // Borramos todos los datos de la sesión actual
            val preferencias = getSharedPreferences("BarberFlowPrefs", Context.MODE_PRIVATE)
            preferencias.edit().clear().apply()

            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

            // Devolvemos al usuario a la pantalla de Login y cerramos esta
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}