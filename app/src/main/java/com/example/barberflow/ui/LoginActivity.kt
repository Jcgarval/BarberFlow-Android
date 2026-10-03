package com.example.barberflow.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.barberflow.R
import com.example.barberflow.api.RetrofitClient
import com.example.barberflow.models.LoginRequest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Comprobamos la caja fuerte, pero ahora buscamos el TOKEN de seguridad
        val preferencias = getSharedPreferences("BarberFlowPrefs", MODE_PRIVATE)
        val tokenGuardado = preferencias.getString("TOKEN", null)
        val rolGuardado = preferencias.getString("ROL", "cliente")

        // Si ya hay un Token válido guardado, saltamos directos según el rol
        if (tokenGuardado != null) {
            redirigirSegunRol(rolGuardado!!)
            return
        }

        // 2. Si no hay sesión activa, cargamos la pantalla
        setContentView(R.layout.activity_login)

        val inputEmail = findViewById<EditText>(R.id.et_email)
        val inputPassword = findViewById<EditText>(R.id.et_password)
        val btnEntrar = findViewById<Button>(R.id.btn_entrar)

        val api = RetrofitClient.getApi(this)

        btnEntrar.setOnClickListener {
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                btnEntrar.isEnabled = false
                btnEntrar.text = getString(R.string.login_iniciando_sesion)

                lifecycleScope.launch {
                    try {
                        val credenciales = LoginRequest(email, password)
                        val respuesta = api.login(credenciales)

                        // ¡Éxito! Guardamos todos los datos que nos devuelve el servidor, incluido el Token
                        preferencias.edit().apply {
                            putString("TOKEN", respuesta.access_token)
                            putString("ROL", respuesta.rol)
                            putInt("CLIENTE_ID", respuesta.id)
                            putString("CLIENTE_NOMBRE", respuesta.nombre)
                            putString("CLIENTE_EMAIL", email)
                            apply()
                        }

                        Toast.makeText(this@LoginActivity, getString(R.string.login_bienvenido, respuesta.nombre), Toast.LENGTH_SHORT).show()

                        redirigirSegunRol(respuesta.rol)

                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, R.string.login_correo_o_contrasena_incorrectos, Toast.LENGTH_LONG).show()
                        btnEntrar.isEnabled = true
                        btnEntrar.text = getString(R.string.login_entrar)
                    }
                }
            } else {
                Toast.makeText(this, R.string.login_por_favor_escribe_tu_correo_y_contrasena, Toast.LENGTH_SHORT).show()
            }
        }
        val tvIrRegistro = findViewById<TextView>(R.id.tv_ir_registro)

        tvIrRegistro.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun redirigirSegunRol(rol: String) {
        if (rol == "admin") {
            Toast.makeText(this, R.string.login_modo_administrador_activado, Toast.LENGTH_LONG).show()

            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
}