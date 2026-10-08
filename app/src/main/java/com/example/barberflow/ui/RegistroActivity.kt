package com.example.barberflow.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

// Importaciones de nuestro proyecto
import com.example.barberflow.R
import com.example.barberflow.api.RetrofitClient
import com.example.barberflow.models.ClienteCreate
import com.example.barberflow.models.errorDePassword
import com.example.barberflow.models.mensajeDeError
import com.google.android.material.appbar.MaterialToolbar
import retrofit2.HttpException

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Que el contenido no quede bajo la barra de estado en Android recientes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }
        findViewById<MaterialToolbar>(R.id.toolbar_registro).setNavigationOnClickListener { finish() }

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

            // Las mismas reglas que valida el servidor, para avisar antes de enviar nada
            val errorPassword = errorDePassword(password)

            if (nombre.length > 60) {
                Toast.makeText(this, R.string.registro_nombre_largo, Toast.LENGTH_SHORT).show()
            } else if (nombre.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && errorPassword != null) {
                Toast.makeText(this, errorPassword, Toast.LENGTH_SHORT).show()
            } else if (nombre.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                btnRegistrar.isEnabled = false
                btnRegistrar.text = getString(R.string.registro_creando_cuenta)

                lifecycleScope.launch {
                    try {
                        val nuevoCliente = ClienteCreate(nombre, email, password)
                        api.crearCliente(nuevoCliente)

                        Toast.makeText(this@RegistroActivity, R.string.registro_cuenta_creada_ya_puedes_iniciar_sesion, Toast.LENGTH_LONG).show()

                        // Cerramos esta pantalla para devolver al usuario al Login
                        finish()
                    } catch (e: Exception) {
                        // Esto imprimirá el error real en la pestaña Logcat de Android Studio
                        Log.e("BarberFlowError", "Fallo exacto al registrar: ", e)

                        // Esto te lo mostrará en el móvil
                        val mensaje = if (e is HttpException) mensajeDeError(this@RegistroActivity, e) else getString(R.string.comun_no_se_pudo_conectar_con_el_servidor)
                        Toast.makeText(this@RegistroActivity, mensaje, Toast.LENGTH_LONG).show()

                        btnRegistrar.isEnabled = true
                        btnRegistrar.text = getString(R.string.registro_registrarme)
                    }
                }
            } else {
                Toast.makeText(this, R.string.registro_rellena_todos_los_campos, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
