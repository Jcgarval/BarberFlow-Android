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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Comprobar si ya estamos registrados (Mirar en la "caja fuerte" de Android)
        val preferencias = getSharedPreferences("BarberFlowPrefs", Context.MODE_PRIVATE)
        val idGuardado = preferencias.getInt("CLIENTE_ID", -1)

        // Si ya hay un ID guardado (es distinto de -1), saltamos directos a las reservas
        if (idGuardado != -1) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // 2. Si no hay ID, mostramos la pantalla de Login
        setContentView(R.layout.activity_login)

        val inputNombre = findViewById<EditText>(R.id.et_nombre_cliente)
        val btnEntrar = findViewById<Button>(R.id.btn_entrar)

        // Configurar Retrofit (Recuerda poner tu IP)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.23:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(BarberiaApi::class.java)

        btnEntrar.setOnClickListener {
            val nombre = inputNombre.text.toString().trim()

            if (nombre.isNotEmpty()) {
                // Bloqueamos el botón para evitar doble clic
                btnEntrar.isEnabled = false
                btnEntrar.text = "Registrando..."

                lifecycleScope.launch {
                    try {
                        // Enviamos el nombre al backend (ponemos un teléfono de relleno por ahora)
                        val nuevoCliente = ClienteCreate(nombre = nombre, telefono = "000000000")
                        val respuesta = api.crearCliente(nuevoCliente)

                        // ¡Éxito! Guardamos el ID real que nos dio la base de datos en la "caja fuerte"
                        preferencias.edit().putInt("CLIENTE_ID", respuesta.id).apply()
                        preferencias.edit().putString("CLIENTE_NOMBRE", respuesta.nombre).apply()

                        Toast.makeText(this@LoginActivity, "¡Bienvenido, ${respuesta.nombre}!", Toast.LENGTH_SHORT).show()

                        // Saltamos a la pantalla de reservas
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Error al conectar con el servidor", Toast.LENGTH_LONG).show()
                        btnEntrar.isEnabled = true
                        btnEntrar.text = "Entrar"
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, escribe tu nombre", Toast.LENGTH_SHORT).show()
            }
        }
    }
}