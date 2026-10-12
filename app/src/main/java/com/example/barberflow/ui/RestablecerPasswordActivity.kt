package com.example.barberflow.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.barberflow.R
import com.example.barberflow.api.RetrofitClient
import com.example.barberflow.models.ProblemaPassword
import com.example.barberflow.models.RestablecerPasswordRequest
import com.example.barberflow.models.SolicitudRecuperacion
import com.example.barberflow.models.esCodigoValido
import com.example.barberflow.models.mensajeDeError
import com.example.barberflow.models.problemaDePassword
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Segundo paso: el usuario escribe el código que le llegó por correo y la contraseña nueva. */
class RestablecerPasswordActivity : AppCompatActivity() {

    private var temporizador: CountDownTimer? = null
    private lateinit var btnReenviar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restablecer_password)

        val email = intent.getStringExtra(EXTRA_EMAIL).orEmpty()
        if (email.isEmpty()) {
            finish()
            return
        }

        // Que el contenido no quede bajo la barra de estado en Android recientes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }
        findViewById<MaterialToolbar>(R.id.toolbar_restablecer).setNavigationOnClickListener { finish() }
        findViewById<TextView>(R.id.tv_restablecer_info).text = getString(R.string.restablecer_info, email)

        val campoCodigo = findViewById<TextInputLayout>(R.id.til_codigo)
        val campoNueva = findViewById<TextInputLayout>(R.id.til_nueva_password)
        val campoRepetir = findViewById<TextInputLayout>(R.id.til_repetir_password)
        val etCodigo = findViewById<TextInputEditText>(R.id.et_codigo)
        val etNueva = findViewById<TextInputEditText>(R.id.et_nueva_password)
        val etRepetir = findViewById<TextInputEditText>(R.id.et_repetir_password)
        val btnCambiar = findViewById<Button>(R.id.btn_cambiar_password)
        btnReenviar = findViewById(R.id.btn_reenviar_codigo)
        val api = RetrofitClient.getApi(this)

        etCodigo.doAfterTextChanged { campoCodigo.error = null }
        etNueva.doAfterTextChanged { campoNueva.error = null }
        etRepetir.doAfterTextChanged { campoRepetir.error = null }

        // El código acaba de enviarse: hasta pasado un minuto el servidor no genera otro
        iniciarEspera()

        btnCambiar.setOnClickListener {
            val codigo = etCodigo.text.toString().trim()
            val nueva = etNueva.text.toString().trim()
            val repetida = etRepetir.text.toString().trim()

            var valido = true
            if (!esCodigoValido(codigo)) {
                campoCodigo.error = getString(R.string.restablecer_error_codigo)
                valido = false
            }
            val problema = problemaDePassword(nueva)
            if (problema != null) {
                campoNueva.error = textoDelProblema(problema)
                valido = false
            } else if (nueva != repetida) {
                campoRepetir.error = getString(R.string.restablecer_error_no_coinciden)
                valido = false
            }
            if (!valido) return@setOnClickListener

            btnCambiar.isEnabled = false
            btnCambiar.text = getString(R.string.restablecer_cambiando)

            lifecycleScope.launch {
                try {
                    val respuesta = api.restablecerPassword(RestablecerPasswordRequest(email, codigo, nueva))
                    if (respuesta.isSuccessful) {
                        Toast.makeText(this@RestablecerPasswordActivity, R.string.restablecer_ok, Toast.LENGTH_LONG).show()
                        setResult(RESULT_OK)
                        finish()
                        return@launch
                    }
                    val mensaje = mensajeDeError(this@RestablecerPasswordActivity, respuesta)
                    if (respuesta.code() == 400) {
                        campoCodigo.error = mensaje // código incorrecto, caducado o ya usado
                    } else {
                        Toast.makeText(this@RestablecerPasswordActivity, mensaje, Toast.LENGTH_LONG).show()
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Toast.makeText(this@RestablecerPasswordActivity, R.string.comun_no_se_pudo_conectar_con_el_servidor, Toast.LENGTH_LONG).show()
                }
                btnCambiar.isEnabled = true
                btnCambiar.text = getString(R.string.restablecer_cambiar)
            }
        }

        btnReenviar.setOnClickListener {
            btnReenviar.isEnabled = false
            lifecycleScope.launch {
                try {
                    val respuesta = api.olvidePassword(SolicitudRecuperacion(email))
                    if (respuesta.isSuccessful) {
                        Toast.makeText(this@RestablecerPasswordActivity, R.string.restablecer_codigo_reenviado, Toast.LENGTH_LONG).show()
                        iniciarEspera()
                    } else {
                        Toast.makeText(this@RestablecerPasswordActivity, mensajeDeError(this@RestablecerPasswordActivity, respuesta), Toast.LENGTH_LONG).show()
                        btnReenviar.isEnabled = true
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Toast.makeText(this@RestablecerPasswordActivity, R.string.comun_no_se_pudo_conectar_con_el_servidor, Toast.LENGTH_LONG).show()
                    btnReenviar.isEnabled = true
                }
            }
        }
    }

    private fun textoDelProblema(problema: ProblemaPassword): String = getString(
        when (problema) {
            ProblemaPassword.CORTA -> R.string.restablecer_error_corta
            ProblemaPassword.LARGA -> R.string.restablecer_error_larga
            ProblemaPassword.SIN_LETRA_O_NUMERO -> R.string.restablecer_error_letra_numero
        }
    )

    /** Deshabilita "Reenviar código" durante un minuto, con la cuenta atrás en el propio botón. */
    private fun iniciarEspera() {
        temporizador?.cancel()
        btnReenviar.isEnabled = false
        temporizador = object : CountDownTimer(ESPERA_REENVIO_SEGUNDOS * 1000L, 1000L) {
            override fun onTick(milisRestantes: Long) {
                val segundos = ((milisRestantes + 999) / 1000).toInt()
                btnReenviar.text = getString(R.string.restablecer_reenviar_espera, segundos)
            }

            override fun onFinish() {
                btnReenviar.text = getString(R.string.restablecer_reenviar)
                btnReenviar.isEnabled = true
            }
        }.start()
    }

    override fun onDestroy() {
        temporizador?.cancel()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_EMAIL = "EMAIL"
        private const val ESPERA_REENVIO_SEGUNDOS = 60 // igual que la espera mínima entre códigos del servidor
    }
}