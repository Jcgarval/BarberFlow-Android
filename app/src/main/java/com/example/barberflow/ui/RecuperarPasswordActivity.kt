package com.example.barberflow.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.barberflow.R
import com.example.barberflow.api.RetrofitClient
import com.example.barberflow.models.SolicitudRecuperacion
import com.example.barberflow.models.mensajeDeError
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Primer paso de "¿Olvidaste tu contraseña?": el usuario escribe su correo y el servidor le envía un código. */
class RecuperarPasswordActivity : AppCompatActivity() {

    // Si la contraseña se cambia en la pantalla del código, volvemos directamente al login
    private val pantallaDelCodigo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        if (resultado.resultCode == RESULT_OK) finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_password)

        // Que el contenido no quede bajo la barra de estado en Android recientes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }
        findViewById<MaterialToolbar>(R.id.toolbar_recuperar).setNavigationOnClickListener { finish() }

        val campoEmail = findViewById<TextInputLayout>(R.id.til_recuperar_email)
        val etEmail = findViewById<TextInputEditText>(R.id.et_recuperar_email)
        val btnEnviar = findViewById<Button>(R.id.btn_enviar_codigo)
        val api = RetrofitClient.getApi(this)

        // Si el usuario ya había escrito su correo en el login, lo reutilizamos
        etEmail.setText(intent.getStringExtra(EXTRA_EMAIL).orEmpty())
        etEmail.doAfterTextChanged { campoEmail.error = null }

        btnEnviar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            when {
                email.isEmpty() -> campoEmail.error = getString(R.string.recuperar_escribe_tu_correo)
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> campoEmail.error = getString(R.string.recuperar_correo_no_valido)
                else -> {
                    btnEnviar.isEnabled = false
                    btnEnviar.text = getString(R.string.recuperar_enviando)

                    lifecycleScope.launch {
                        try {
                            val respuesta = api.olvidePassword(SolicitudRecuperacion(email))
                            if (respuesta.isSuccessful) {
                                // El servidor responde igual exista o no la cuenta: siempre pasamos a la pantalla del código
                                val pantalla = Intent(this@RecuperarPasswordActivity, RestablecerPasswordActivity::class.java)
                                pantalla.putExtra(RestablecerPasswordActivity.EXTRA_EMAIL, email)
                                pantallaDelCodigo.launch(pantalla)
                            } else {
                                Toast.makeText(this@RecuperarPasswordActivity, mensajeDeError(this@RecuperarPasswordActivity, respuesta), Toast.LENGTH_LONG).show()
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Toast.makeText(this@RecuperarPasswordActivity, R.string.comun_no_se_pudo_conectar_con_el_servidor, Toast.LENGTH_LONG).show()
                        }
                        btnEnviar.isEnabled = true
                        btnEnviar.text = getString(R.string.recuperar_enviar_codigo)
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_EMAIL = "EMAIL"
    }
}