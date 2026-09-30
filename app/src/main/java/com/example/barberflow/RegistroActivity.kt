package com.example.barberflow

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val etNombre = findViewById<EditText>(R.id.et_registro_nombre)
        val etEmail = findViewById<EditText>(R.id.et_registro_email)
        val etPassword = findViewById<EditText>(R.id.et_registro_password)
        val btnRegistrar = findViewById<Button>(R.id.btn_registrar)

        // Usamos nuestro cliente centralizado para no requerir token al registrar
        val api = RetrofitClient.getApi(this)

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (nombre.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                btnRegistrar.isEnabled = false
                btnRegistrar.text = "Creando cuenta..."

                lifecycleScope.launch {
                    try {
                        val nuevoCliente = ClienteCreate(nombre, email, password)
                        api.crearCliente(nuevoCliente)

                        Toast.makeText(this@RegistroActivity, "Cuenta creada. ¡Ya puedes iniciar sesión!", Toast.LENGTH_LONG).show()

                        // Cerramos esta pantalla para devolver al usuario al Login
                        finish()
                    } catch (e: Exception) {
                        // Esto imprimirá el error real en la pestaña Logcat de Android Studio
                        android.util.Log.e("BarberFlowError", "Fallo exacto al registrar: ", e)

                        // Esto te lo mostrará en el móvil
                        Toast.makeText(this@RegistroActivity, "Error real: ${e.message}", Toast.LENGTH_LONG).show()

                        btnRegistrar.isEnabled = true
                        btnRegistrar.text = "Registrarme"
                    }
                }
            } else {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}